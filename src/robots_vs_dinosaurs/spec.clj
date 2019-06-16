(ns robots-vs-dinosaurs.spec
  "Clojure specs for project domain model."
  (:require (robots-vs-dinosaurs.logic
              [direction :refer [four-sides]]
              [unit :refer [dinosaur-subtype]])
            (clojure.spec
              [alpha :as s])
            (clojure.test.check
              [generators :as gen])))

;;;;;;;;;;;;;;;;;;
;;
;; Logic Spec
;;

;;
;; Direction Spec
;;
(s/def :direction/orientation (set (keys four-sides)))
(s/def :direction/point (set (vals four-sides)))

(s/def :direction/direction
  (s/and
    (s/keys :req-un [:direction/orientation :direction/point])
    (fn [{:keys [orientation point]}]
      (= (orientation four-sides) point))))

;;
;; Size Spec
;;
(s/def :size/width pos-int?)
(s/def :size/height pos-int?)
(s/def :size/size
  (s/keys :req-un [:size/width :size/height]))

;;
;; Point Spec
;;
(s/def :point/x int?)
(s/def :point/y int?)
(s/def :point/point
  (s/keys :req-un [:point/x :point/y]))

;;
;; Unit Spec
;;
(s/def :unit/id nat-int?)
(s/def :unit/type #{:dinosaur :robot})

(s/def :dinosaur/subtype dinosaur-subtype)
(s/def :unit/dinosaur
  (s/and
    (s/keys :req-un [:unit/id :unit/type :point/point :dinosaur/subtype])
    (fn [{:keys [type]}]
      (= :dinosaur type))))

(s/def :unit/dinosaurs
  (s/* :unit/dinosaur))

(s/def :unit/robot
  (s/and
    (s/keys :req-un [:unit/id :unit/type :point/point :direction/direction])
    (fn [{:keys [type]}]
      (= :robot type))))

(s/def :unit/robots
  (s/* :unit/robot))

;;
;; Board Spec
;;
(def ^:const min-size 5)
(def ^:const max-size 50)

(s/def :board/width (s/int-in min-size max-size))
(s/def :board/height (s/int-in min-size max-size))
(s/def :board/size
  (s/keys :req-un [:board/width :board/height]))

(s/def :board/unit
  (s/or :robot :unit/robot :dinosaur :unit/dinosaur))

(s/def :board/units
  (s/and (s/coll-of :board/unit :kind set?)))

(s/def :board/board
  (s/keys :req-un [:board/size]
          :opt-un [:board/units]))

;;
;; Scoreboard Spec
;;
(s/def :scoreboard/total int?)
(s/def :scoreboard/scoreboard
  (s/keys :req-un [:scoreboard/total]))

;;
;; Simulation Spec
;;
(s/def :simulation/id nat-int?)
(s/def :simulation/title (s/and string? #(> (count %) 2)))

(s/def :simulation/simulation
  (s/keys :req-un [:simulation/id
                   :simulation/title
                   :scoreboard/scoreboard
                   :board/board]))

(s/def :simulation/simulations
  (s/* :simulation/simulation))

;;;;;;;;;;;;;;;;;;
;;
;; Custom Generators
;;
(defn gen-string-pos-int
  "Generates a string positive integer."
  []
  (gen/fmap str gen/pos-int))

;;;;;;;;;;;;;;;;;;
;;
;; Request Spec
;;

;; Request Point Spec
(s/def :request/x (s/int-in 0 (dec max-size)))
(s/def :request/y (s/int-in 0 (dec max-size)))

(s/def :request/point
  (s/keys :req-un [:request/x :request/y]))

;; Request Direction Spec
(s/def :request/orientation (set (map name (keys four-sides))))
(s/def :request/direction
  (s/keys :req-un [:request/point :request/orientation]))

;; Request Size Spec
(s/def :request/width (s/int-in min-size max-size))
(s/def :request/height (s/int-in min-size max-size))

(s/def :request/size
  (s/keys :req-un [:request/width :request/height]))

;; Request Robot Spec
(s/def :request/robot
  (s/keys :req-un [:request/point :request/orientation]))

;; Request Dinosaur Spec
(s/def :request/subtype (set (map name dinosaur-subtype)))

(s/def :request/dinosaur
  (s/keys :req-un [:request/point :request/subtype]))

(s/def :request/title (s/and string? not-empty))

(s/def :request/simulation
  (s/keys :req-un [:request/size :request/title]))

;;;;;;;;;;;;;;;;;;
;;
;; Response Spec
;;

(s/def :response/robot-attack
  (s/map-of #{:attacked} :unit/dinosaurs))

;; Success
(s/def :response/success
  (s/map-of #{:success} boolean?))

;; Error
(s/def :response/code nat-int?)
(s/def :response/status nat-int?)
(s/def :response/message string?)
(s/def :response/error
  (s/keys :req-un [:response/code
                   :response/status
                   :response/message]))