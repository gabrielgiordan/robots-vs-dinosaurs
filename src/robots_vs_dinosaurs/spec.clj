(ns robots-vs-dinosaurs.spec
  "Clojure specs for project domain model."
  (:require [clojure.spec.alpha :as s]
            [robots-vs-dinosaurs.util :as u]))

(defonce
  four-directions
  {:up    [0 1]
   :right [1 0]
   :down  [0 -1]
   :left  [-1 0]})

(defonce
  eight-directions
  {:up-left    [-1 1]
   :up         [0 1]
   :up-right   [1 1]
   :right      [1 0]
   :down-right [1 -1]
   :down       [0 -1]
   :down-left  [-1 -1]
   :left       [-1 0]})

(defonce min-size 5)
(defonce max-size 100)

;; Id
(s/def ::id (s/and nat-int?))
(s/def ::ids (s/coll-of ::id))
(s/def ::title string?)

(s/def ::coordinate (s/int-in 0 (dec max-size)))
(s/def ::x ::coordinate)
(s/def ::y ::coordinate)
(s/def ::point (s/keys :req-un [::x ::y]))

(s/def ::length (s/int-in min-size max-size))
(s/def ::width ::length)
(s/def ::height ::length)
(s/def ::size (s/keys :req-un [::width ::height]))

;; Direction
(s/def ::orientation
  (set (vals four-directions)))

(s/def ::name
  (set (keys four-directions)))

(s/def ::direction
  (s/and
    (s/keys :req-un [::name ::orientation])
    (fn [{:keys [name orientation]}]
      (= (name four-directions) orientation))))

;; Unit
(s/def ::type
  #{:robot :dinosaur})

(s/def ::score int?)

(s/def ::unit
  (s/keys :req-un [::id ::point]))

(s/def ::dinosaur (s/keys :req-un [::id ::point]))
(s/def ::robot (s/keys :req-un [::id ::point ::direction ::score]))

(s/def ::units
  (s/coll-of
    (s/or :robot ::robot
          :dinosaur ::dinosaur)))

;; Board
(s/def ::board
  (s/keys :req-un [::size]
          :opt-un [::units]))

;; Scoreboard
(s/def ::total int?)

(s/def ::scoreboard
  (s/keys :req-un [::total]))

;; Simulation
(s/def ::simulation
  (s/keys :req-un [::id ::title ::scoreboard ::board]))

(s/def ::simulations (s/coll-of ::simulation))

;; Request
(s/def ::post-simulation
  (s/keys :req-un [::title]
          :opt-un [::size]))

