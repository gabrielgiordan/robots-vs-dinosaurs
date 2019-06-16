(ns robots-vs-dinosaurs.logic.board
  (:require
    (clojure
      [set :refer [difference]])
    (robots-vs-dinosaurs
      [util :refer [find-first]])
    (robots-vs-dinosaurs.logic
      [unit :as unit])))

;;
;; Board
;;
(defrecord Board [size units])

(defn new-board
  "Creates a new board."
  ([size units]
   (map->Board
     {:size  size
      :units units}))
  ([size]
   (new-board size #{}))
  ([]
   (new-board [50 50])))

;;
;; Units
;;
(defn message?
  "Checks if the result is a
  message to be handled afterwards."
  [result]
  (keyword? result))

(defn outbound?
  "Check if a point is out of bounds of the board."
  [{{:keys [width height]} :size} {:keys [x y]}]
  (not (and (>= x 0) (>= y 0) (< x width) (< y height))))

(defn get-unit
  "Gets a unit with `id`."
  [{:keys [units]} id]
  (find-first units [:id id]))

(defn get-unit-at
  "Gets a unit at a specific point."
  [{:keys [units]} point]
  (find-first units [:point point]))

(defn occupied?
  "Check if a point is occupied."
  [board point]
  (boolean (get-unit-at board point)))

(defn add-unit
  "Adds an unit inside the board and
  returns an map with `:updated` board and
  the `:response` with the unit, else
  returns a message `:out-of-bounds` or `:occupied`."
  [{:keys [units] :as board} {:keys [point] :as unit}]
  (condp #(%1 board %2) point
    outbound? :out-of-bounds
    occupied? :occupied
    {:updated  (assoc-in board [:units] (conj units unit))
     :response unit}))

(defn update-unit
  "Update when find the unit and returns a map
  with the actual `update` and the `response`.
  Decided to implement this when after reading
  a discussion on how to handle return types,
  see: https://bit.ly/2EWX3eU
  TODO: learn cats and use the monad implementations."
  [{:keys [units] :as board} unit new-unit]
  (when unit
    {:updated  (assoc board :units (conj (disj units unit) new-unit))
     :response new-unit}))

;;
;; Unit Rotate
;;
(defn- turn-unit
  "Turns a unit with the given `turn` function
  with takes `unit` as argument."
  [board unit turn]
  (update-unit board unit (turn unit)))

(defn- turn-unit-right-4
  "Turns a unit right with 4 directions side."
  [board unit]
  (turn-unit board unit unit/turn-right-4))

(defn- turn-unit-left-4
  "Turns a unit left with 4 directions side."
  [board unit]
  (turn-unit board unit unit/turn-left-4))

;;
;; Unit Move
;;
(defn- move-unit-handler
  "Handles the unit movement:
  when outbound of the board, returns `:outbound`,
  when already occupied, returns `:occupied`,
  else returns the updated `unit`."
  [board]
  (fn [unit point]
    (condp #(%1 board %2) point
      outbound? :out-of-bounds
      occupied? :occupied
      (assoc unit :point point))))

(defn- move-unit
  "Do moves a unit,
  when not invalid operation."
  [board unit move]
  (when-let [result (move unit (move-unit-handler board))]
    (if (message? result)
      result
      (update-unit board unit result))))

(defn- move-unit-forward
  "Moves the unit forward,
  when at a valid point."
  [board unit]
  (move-unit board unit unit/move-forward))

(defn- move-unit-backward
  "Moves the unit backward,
  when at a valid point."
  [board unit]
  (move-unit board unit unit/move-backward))

;;
;; Unit Attack
;;
(defn- unit-attack-handler
  "Handles an unit attack, checking if
  the attacked point is `outbound` or has
  no a valid unit on it."
  [board validation-handler]
  (fn [point]
    (when-not (outbound? board point)
      (some->
        (get-unit-at board point)
        (validation-handler)))))

(defn- unit-attack
  "Used in a update function, makes a unit attack
  when a valid attacked unit is encountered.
  Returns an `:attack-missed` if no units are
  find, else returns a map with the `:updated`
  board and the `:response` with the attacked units."
  [attack validation-handler]
  (fn [{:keys [units] :as board} unit]
    (attack
      unit
      (fn [points]
        (as->
          (map (unit-attack-handler board validation-handler) points)
          attacked
          (filter identity attacked)
          (if (not-empty attacked)
            {:updated  (assoc board :units (difference units attacked))
             :response attacked}
            :attack-missed))))))

;;
;; Robot
;;
(defn add-robot
  "Adds a robot to the board."
  [board robot]
  (add-unit board robot))

(defn get-robots
  "Gets all robots inside the board."
  [{:keys [units]}]
  (filter unit/robot? units))

(defn get-robot
  "Gets a robot into the board with the given `id`,
  when not a robot, returns the message `:not-a-robot`,
  else return `nil` when nothing has found."
  [board robot-id]
  (when-let [unit (get-unit board robot-id)]
    (if (unit/robot? unit) unit :not-a-robot)))

(defn- update-robot
  "Updates a robot when has a valid result.
  Returns a map with the `response`
  and the actual `update`, or a keyword `message` to be handled."
  [board robot-id f]
  (when-let [result (get-robot board robot-id)]
    (if (message? result) result (f board result))))

(defn- robot-attack-validation
  "Validate the robot attack, checking when
  the validated unit is a `dinosaur` to continue
  the attack."
  [unit]
  (when (unit/dinosaur? unit) unit))

(defn robot-attack
  "Makes a robot attack dinosaurs around it.
  If the attack misses, return `:missed-attack` message,
  else returns a map with the `response` with the
  attacked dinosaurs and the `updated` board."
  [board robot-id]
  (update-robot
    board
    robot-id
    (unit-attack unit/attack-4-directions robot-attack-validation)))

(defn move-robot-forward
  "Moves a robot forward,
  when at a valid point."
  [board robot-id]
  (update-robot board robot-id move-unit-forward))

(defn move-robot-backward
  "Moves a robot backward,
  when at a valid point."
  [board robot-id]
  (update-robot board robot-id move-unit-backward))

(defn turn-robot-right
  "Turns a robot to the right."
  [board robot-id]
  (update-robot board robot-id turn-unit-right-4))

(defn turn-robot-left
  "Turns a robot to the left."
  [board robot-id]
  (update-robot board robot-id turn-unit-left-4))

;;
;; Dinosaur
;;
(defn add-dinosaur
  "Adds a dinosaur to the board."
  [board dinosaur]
  (add-unit board dinosaur))

(defn get-dinosaurs
  "Gets all the dinosaurs inside the board."
  [{:keys [units]}]
  (filter unit/dinosaur? units))

(defn get-dinosaur
  "Gets a dinosaur into the board with the given `id`,
  when not a dinosaur, returns the message `:not-a-dinosaur`,
  else return `nil` when nothing has found."
  [board robot-id]
  (when-let [unit (get-unit board robot-id)]
    (if (unit/dinosaur? unit) unit :not-a-dinosaur)))