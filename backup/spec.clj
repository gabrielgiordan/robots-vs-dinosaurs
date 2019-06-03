(ns robots-vs-dinosaurs.spec
  "Clojure Specs for Robots vs Dinosaurs domain."
  (:require [clojure.spec.alpha :as s]
            [robots-vs-dinosaurs.util :as u]))

(defonce four-directions
         {:up    [0 1]
          :right [1 0]
          :down  [0 -1]
          :left  [-1 0]})

(defonce eight-directions
         {:up-left    [-1 1]
          :up         [0 1]
          :up-right   [1 1]
          :right      [1 0]
          :down-right [1 -1]
          :down       [0 -1]
          :down-left  [-1 -1]
          :left       [-1 0]})

(defonce min-size 5)
(defonce max-size 60)

;; Id
(s/def ::id (s/and pos-int?))

;; Direction
(s/def ::facing (set (vals four-directions)))
(s/def ::orientation (set (keys four-directions)))
(s/def ::direction (s/and (s/keys :req-un [::orientation ::facing])
                          (u/key-value-of four-directions)))

;; Position
(s/def ::point (s/coll-of (s/and int? #(>= % 0)) :kind vector? :count 2))

;; Unit
(s/def ::score int?)
(s/def ::type #{:robot :dinosaur})

(s/def ::unit (s/keys :req-un [::id ::point]))

(s/def ::dinosaur ::unit)
(s/def ::robot (s/merge ::unit (s/keys :req-un [::direction ::score])))

(s/def ::units (s/coll-of
                 (s/or :robot ::robot
                       :dinosaur ::dinosaur) :kind set?))

;; Size
(s/def ::size (s/coll-of (s/int-in min-size max-size) :kind vector? :count 2))

;; Board
(s/def ::board (s/keys :req-un [::size ::units]))

;; Scoreboard
(s/def ::total int?)
(s/def ::scoreboard (s/keys :req-un [::total]))

;; Simulation
(s/def ::simulation (s/keys :req-un [::id ::scoreboard ::board]))
(s/def ::simulations (s/coll-of ::simulation :kind map?))

