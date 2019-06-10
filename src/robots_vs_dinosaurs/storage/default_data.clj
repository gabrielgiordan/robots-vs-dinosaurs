(ns robots-vs-dinosaurs.storage.default-data
  (:require
    (robots-vs-dinosaurs.logic
      [simulation :as simulation]
      [board :as board]
      [size :as size]
      [unit :as unit]
      [point :as point])))

; Aliases
(def ^:const s simulation/new-simulation)
(def ^:const b board/new-board)
(def ^:const z size/new-size)
(def ^:const p point/new-point)
(def ^:const r unit/new-robot)
(def ^:const d unit/new-dinosaur)

(defn get-default-simulations
  []
  {0 (s 0 "Aerodynamic Chinchilla"
        (b
          (z 20 20)
          #{(r 1 (p 0 0) :up)
            (d 2 (p 1 0))}))

   3 (s 3 "Heavy Voracious Bunny Buffalo"
        (b
          (z 50 50)
          #{(r 4 (p 2 5) :left)
            (d 5 (p 3 5))
            (d 6 (p 1 5))
            (d 7 (p 3 6))
            (d 8 (p 3 4))}))

   9 (s 9 "Our Arcadian GPU")})

(defn get-default-id
  []
  10)

(defn get-default-data
  []
  {:simulations (get-default-simulations)
   :identifiers (get-default-id)})