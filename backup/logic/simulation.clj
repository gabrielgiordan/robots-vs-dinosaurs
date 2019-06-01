(ns robots-vs-dinosaurs.logic.simulation
  (:require [robots-vs-dinosaurs.spec :as spec]
            [robots-vs-dinosaurs.util :as u]
            [nano-id.custom :refer [generate]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defrecord Simulation [id scoreboard board])
(defrecord Scoreboard [total])
(defrecord Board [size units])
(defrecord Direction [orientation facing])
(defrecord Dinosaur [id position])
(defrecord Robot [id position direction score])

;; Could be use separate `ref`s, but instead,
;; I choose to use a single immutable state approach.
(def simulations-atom (atom {}))

;(defn- random-size
;  []
;  (gen/generate (s/gen ::spec/size)))

;;

;; Exceptions
(defonce out-of-bonds-ex (ex-info "Position is out of bounds." {}))
(defonce occupied-ex (ex-info "Position occupied." {}))
(defonce not-found-ex (ex-info "Not found." {}))
(defonce not-instance-ex (ex-info "Invalid instance." {}))

(def custom-nano-id (generate "1234567890"))

(defn search-id
  [id]
  {:id id})

(defn search-position
  [position]
  {:position position})

;; Id
(defn random-id
  []
  (custom-nano-id 1))

;; Records
(defn simulation-create-record
  [id size]
  (->Simulation id (->Scoreboard 0) (->Board size #{})))

(defn direction-create-record
  [orientation directions]
  (->Direction orientation (orientation directions)))

(defn dinosaur-create-record
  [id position]
  (->Dinosaur id position))

(defn robot-create-record
  [id position orientation directions]
  (->Robot id position (direction-create-record orientation directions) 0))

;; Position
(defn- position-inbound?
  "Returns `true` when `x` and `y` is inbound."
  [[width height] [x y :as position]]
  (and (every? #(>= % 0) position) (< x width) (< y height)))

(defn- position+
  [a b]
  (mapv + a b))

(defn- position-
  [a b]
  (mapv - a b))

(defn- positions+
  [positions position]
  (mapv (partial position+ position) positions position))

;; Direction
(defn- direction-index
  [directions {:keys [orientation]}]
  (first (take 1 (keep-indexed #(when (= orientation (key %2)) %1) directions))))

(defn- direction-orientation-from-index
  [index directions]
  (first (first (take 1 (keep-indexed #(when (= index %1) %2) directions)))))

(defn- direction-update-by-index
  [directions direction on-index]
  (direction-create-record
    (-> (direction-index directions direction)
        (on-index)
        (mod (count directions))
        (direction-orientation-from-index directions))
    directions))

;; memoize after
(defn- direction-inc
  [directions direction]
  (direction-update-by-index directions direction inc))

(defn- direction-dec
  [directions direction]
  (direction-update-by-index directions direction dec))

;; Simulation
(defn- get-simulation
  [simulations id]
  (simulations id))

(defn- simulation-update
  [simulations id on-simulation]
  (if-let [simulation (simulations id)]
    (assoc simulations id (on-simulation simulation))
    (throw not-found-ex)))

(defn simulation-create
  [simulations id size]
  (assoc simulations id (simulation-create-record id size)))

(defn simulation-delete
  [simulations id]
  (dissoc simulations id))

;; Scoreboard
(defn- scoreboard-update-total
  [simulations id on-scoreboard-total]
  (simulation-update simulations id
                     (fn [simulation]
                       (update-in simulation [:scoreboard :total] on-scoreboard-total))))

(defn scoreboard-sum-total
  [simulations id number]
  (scoreboard-update-total simulations id
                           (fn [total]
                             (+ number total))))

(defn scoreboard-reset-total
  [simulations id]
  (scoreboard-update-total simulations id (constantly 0)))

;; Board
(defn- board-update
  [simulations id on-board]
  (simulation-update simulations id
                     (fn [simulation]
                       (update-in simulation #{:board} on-board))))

;; Units
(defn- units-update
  [simulations id on-units]
  (board-update simulations id
                (fn [{:keys [size] :as board}]
                  (update-in board #{:units} on-units size))))

(defn- unit-find
  [simulations id search on-success on-failure]
  (units-update simulations id
                (fn [units size]
                  (let [unit (u/filter-in-first units search)]
                    (if (nil? unit)
                      (on-failure units size)
                      (on-success units unit size))))))

(defn- unit-create
  [simulations id {:keys [position] :as new-unit}]
  (unit-find simulations id [:position position]
             (fn [_units _unit _size]
               ;; Maybe use `failjure` in the future
               ;; for more functional error handling...
               (throw occupied-ex))
             (fn [units size]
               (if (position-inbound? size position)
                 (conj units new-unit)
                 (throw out-of-bonds-ex)))))

(defn- unit-update
  [simulations id search instance on-unit]
  (unit-find simulations id search
             (fn [units unit size]
               (if (instance? instance unit)
                 (on-unit units unit size)
                 (throw not-instance-ex)))
             (fn [_units _size]
               (throw not-found-ex))))

(defn- unit-delete
  [simulations id search instance]
  (unit-update simulations id search instance
               (fn [units unit _size]
                 (disj units unit))))

(defn- unit-update-one
  [simulations id search instance on-unit]
  (unit-update simulations id search instance
               (fn [units unit size]
                 (-> (conj units (on-unit units unit size))
                     (disj unit)))))

;; Unit rotate
(defn- unit-rotate
  [_units {:keys [direction] :as unit} _size on-direction]
  (let [new-direction (on-direction direction)]
    (assoc-in unit #{:direction} new-direction)))

(defn- unit-rotate-right-with-directions
  [directions units unit size]
  (unit-rotate units unit size (partial direction-inc directions)))

(defn- unit-rotate-left-with-directions
  [directions units unit size]
  (unit-rotate units unit size (partial direction-dec directions)))

(defn- unit-rotate-right
  [directions]
  (partial unit-rotate-right-with-directions directions))

(defn- unit-rotate-left
  [directions]
  (partial unit-rotate-left-with-directions directions))

;; Unit - move
(defn- unit-move
  [units {:keys [direction position] :as unit} size on-position]
  (let [new-position (on-position position (:facing direction))]
    (if (position-inbound? size new-position)
      (if (u/contains-in? units [:position new-position])
        (assoc-in unit #{:position} new-position)
        (throw not-found-ex))
      (throw out-of-bonds-ex))))

(defn- unit-move-forward
  [units unit size]
  (unit-move units unit size position+))

(defn- unit-move-backwards
  [units unit size]
  (unit-move units unit size position-))

;;


;(defn- units-keep-in
;  [units key values]
;  (keep
;    (fn [unit]
;      (when ( val (key unit)) unit))
;    units))

;(defn- unit-attack
;  [directions units {:keys [position] :as unit} size]
;  (let [positions (->> (positions+ (vals directions) position)
;                       (filter (partial position-inbound? size)))]
;    )
;  (let [units-to-delete (units-keep-in units :position positions)]))

;; Dinosaur
(defn dinosaur-create
  [simulations id dinosaur-id position]
  (unit-create simulations id (dinosaur-create-record dinosaur-id position)))

(defn dinosaur-delete
  [simulations id dinosaur-id]
  (unit-delete simulations id [:id dinosaur-id] Dinosaur))


;; Robot
(defn robot-create
  [simulations id unit-id position orientation]
  (unit-create simulations id (robot-create-record unit-id position orientation spec/four-directions)))

(defn robot-delete
  [simulations id robot-id]
  (unit-delete simulations id [:id robot-id] Robot))

(defn robot-move-forward
  [simulations id robot-id]
  (unit-update-one simulations id [:id robot-id] Robot unit-move-forward))

(defn robot-move-backwards
  [simulations id robot-id]
  (unit-update-one simulations id [:id robot-id] Robot unit-move-backwards))

(defn robot-rotate-left
  [simulations id robot-id]
  (unit-update-one simulations id [:id robot-id] Robot (unit-rotate-left spec/four-directions)))

(defn robot-rotate-right
  [simulations id robot-id]
  (unit-update-one simulations id [:id robot-id] Robot (unit-rotate-right spec/four-directions)))


;; Impure

;; Simulation
(defn simulation-create!
  []
  (swap! simulations-atom simulation-create (random-id) [10 10]))

(defn simulation-delete!
  [id]
  (swap! simulations-atom simulation-delete id))

;; Score
(defn score-sum!
  [id n]
  (swap! simulations-atom scoreboard-sum-total id n))

(defn score-reset!
  [id]
  (swap! simulations-atom scoreboard-reset-total id))

;; Dinosaur
(defn dinosaur-create!
  [id position]
  (swap! simulations-atom dinosaur-create id (random-id) position))

(defn dinosaur-delete!
  [id dinosaur-id]
  (swap! simulations-atom dinosaur-delete id dinosaur-id))

;; Robot
(defn robot-create!
  [id position orientation]
  (swap! simulations-atom robot-create id (random-id) position orientation))

(defn robot-delete!
  [id robot-id]
  (swap! simulations-atom robot-delete id robot-id))

(defn robot-move-forward!
  [id robot-id]
  (swap! simulations-atom robot-move-forward id robot-id))

(defn robot-move-backwards!
  [id robot-id]
  (swap! simulations-atom robot-move-backwards id robot-id))

(defn robot-turn-right!
  [id robot-id]
  (swap! simulations-atom robot-rotate-right id robot-id))

(defn robot-turn-left!
  [id robot-id]
  (swap! simulations-atom robot-rotate-left id robot-id))

;(defn robot-attack!
;  [id robot-id]
;  (swap! simulations-atom robot-attack id robot-id))

;(defn impure-create-simulation
;  []
;  (dosync
;    (when-let [id (alter simulation-id-ref next-id)]
;      (alter scoreboards-ref create-scoreboard id)
;    )))
;
;(defn create-simulation
;  ([]
;   (create-simulation (random-size)))
;  ([size]
;   (dosync
;     (when-let [id (alter simulation-id-ref inc)]
;       (alter scoreboards-ref merge {id (->Scoreboard 0)})
;       (alter boards-sizes-ref merge {id size})
;       (alter units-ref merge {id {}})))))
;
;(defn get-simulation
;  [id]
;  (when-let [scoreboard (@scoreboards-ref id)]
;    (when-let [size (@boards-sizes-ref id)]
;      (when-let [units (@units-ref id)]
;        (->Simulation id scoreboard (->Board size units))))))
;
;(defn- get-unit-by-position
;  [simulation-id position]
;  (when-let [units (@units-ref simulation-id)]
;    (util/filter-in-first (vals units) :position position)))
;
;(defn- create-unit
;  [simulation-id position constructor-f]
;    (when (empty? (get-unit-by-position simulation-id position))
;      (dosync
;        (when-let [id (alter unit-id-ref inc)]
;          (let [to-merge {simulation-id {id (constructor-f id)}}]
;            (alter units-ref (partial merge-with into) to-merge))))))
;
;(defn- random-position
;  [simulation-id]
;  (when-let [[width height] (@boards-sizes-ref simulation-id)]
;    (let [x (gen/generate (s/gen (s/int-in 0 width)))
;          y (gen/generate (s/gen (s/int-in 0 height)))]
;      [x y])))
;
;(defn create-dinosaur
;  ([simulation-id]
;   (create-dinosaur simulation-id (random-position simulation-id)))
;  ([simulation-id position]
;   (create-unit simulation-id position #(->Dinosaur % position))))
;
;(defn- get-direction
;  [direction]
;  (->Direction direction (direction spec/directions)))
;
;(defn- random-orientation
;  []
;  (gen/generate (s/gen ::spec/orientation)))
;
;(defn create-robot
;  ([simulation-id]
;   (create-robot simulation-id (random-position simulation-id) (random-orientation)))
;  ([simulation-id position orientation]
;   (create-unit simulation-id position #(->Robot % position (get-direction orientation) 0))))
;
;(defn get-robot
;  [simulation-id unit-id]
;  (let [robot ((@units-ref simulation-id) unit-id)
;        position (robot :position)
;        direction (robot :direction)
;        orientation (direction :orientation)
;        facing (direction :facing)
;        score (robot :score)]
;    ()))
;
;;;TODO: rotate left and right, move forward and backwards and attack

;(defn create-direction
;  [direction]
;  (->Direction direction (direction spec/directions)))
;
;(defn create-dinosaur
;  [id position]
;  (->Dinosaur id position))
;
;(defn create-robot
;  [id position orientation]
;  (->Robot id position (create-direction orientation) 0))
;
;(defn create-scoreboard
;  []
;  (->Scoreboard 0))
;
;(defn create-board
;  [size]
;  (->Board size []))
;
;(defn create-simulation
;  [id size]
;  (->Simulation id (create-scoreboard) (create-board size)))
;
;;; Utils
;
;(defn create-with-id
;  "Invokes the `constructor-f` with the
;  incremented id and conjoin into the `coll-ref`."
;  [id-ref coll-ref constructor-f]
;  (dosync
;    (let [id (alter id-ref inc)]
;      (alter coll-ref conj (constructor-f id)))))
;
;(defn find-first-by-id
;  "Gets the first element by `id` in the `set`."
;  [coll id]
;  (util/find-first-by :id id coll))
;
;(defn disj-by-id
;  "Deletes the first element by `id` in the `set`."
;  [coll-ref id]
;  (if-let [element (find-first-by-id @coll-ref id)]
;    (dosync
;      (alter coll-ref disj element))))
;
;(defn assoc-by-element
;  [coll-ref element new-element]
;  (dosync
;    (alter coll-ref (util/conj-disj new-element) element)))
;
;(defn assoc-by-id
;  [coll-ref id value]
;  (if-let [element (find-first-by-id @coll-ref id)]
;    (dosync
;      (alter coll-ref (util/conj-disj value) element))))
;
;(defn get-in-by-id
;  [coll-ref id key]
;  (if-let [element (find-first-by-id @coll-ref id)]
;    (get-in element key nil)))
;
;(defn update-in-by-id
;  "Updates a value inside the `coll-ref` into the first element by `id`."
;  [coll-ref id update-in-ks update-in-val]
;  (if-let [element (find-first-by-id @coll-ref id)]
;    (let [modified (update-in element update-in-ks update-in-val)]
;      (dosync
;        (alter coll-ref (util/conj-disj modified) element)))))
;
;(def simulation-id-ref (ref 0))
;(def simulations-ref (ref #{}))
;
;;; Simulation
;(defn new-simulation
;  [size]
;  (create-with-id simulation-id-ref simulations-ref #(create-simulation % size)))
;
;(defn get-simulation
;  [id]
;  (find-first-by-id @simulations-ref id))
;
;(defn delete-simulation
;  [id]
;  (disj-by-id simulations-ref id))
;
;(defn reset-simulation
;  [id size]
;  (assoc-by-id simulations-ref id (create-simulation id size)))
;
;;; Scoreboard
;(defn update-scoreboard-total
;  [simulation-id update-f]
;  (update-in-by-id simulations-ref simulation-id [:scoreboard :total] update-f))
;
;;; Units
;(def unit-id-atom (atom 0))
;
;(defn position-available?
;  [units position]
;  (nil? (util/find-first-by :position position units)))
;
;(defn valid-position?
;  [[width height] [x y]]
;  (and (every? #(>= % 0) [x y]) (< x width) (< y height)))
;
;(defn valid-orientation?
;  [orientation]
;  (s/valid? ::spec/orientation orientation))
;
;(defn- new-unit
;  [simulation-id constructor-f position]
;  (if-let [simulation (find-first-by-id @simulations-ref simulation-id)]
;    (let [board (:board simulation)
;          size (:size board)
;          units (:units board)]
;      (when (and (valid-position? size position)
;                 (position-available? units position))
;        (let [id (swap! unit-id-atom inc)
;              unit (constructor-f id)
;              new (update-in simulation [:board :units] #(conj % unit))]
;          (assoc-by-element simulations-ref simulation new))))))
;
;(defn new-robot
;  [simulation-id position orientation]
;  (if (valid-orientation? orientation)
;    (new-unit simulation-id #(create-robot % position orientation) position)))
;
;(defn new-dinosaur
;  [simulation-id position]
;  (new-unit simulation-id #(create-dinosaur % position) position))

;(defn update-simulation
;  [simulation]
;  (update-from-coll-sync :id simulations-ref simulation))
;
;(defn get-simulation
;  [id]
;  (get-by :id id @simulations-ref))
;
;(defn- update-simulation-in
;  [id key value]
;  (update-in-from-coll-sync :id id key value simulations-ref))
;
;;(assoc-in g #{:scoreboard} (->Scoreboard 50))
;
;(defn get-all-simulations
;  []
;  @simulations-ref)
;
;;; Robot
;
;(defn add-robot
;  [simulation])
;