(ns robots-vs-dinosaurs.storage.default-data
  (:require
    (robots-vs-dinosaurs.logic
      [simulation :refer [new-simulation]]
      [board :refer [new-board]]
      [size :refer [new-size]]
      [unit :refer [new-robot new-dinosaur]]
      [point :refer [new-point]])))

; Aliases
(def ^:const s new-simulation)
(def ^:const b new-board)
(def ^:const z new-size)
(def ^:const p new-point)
(def ^:const r new-robot)
(def ^:const d new-dinosaur)

(defn get-default-simulations
  []
  {0 (s 0 "Aerodynamic chinchilla"
        (b
          (z 20 20)
          #{(r 1 (p 0 0) :up)
            (d 2 (p 1 0))}))

   3 (s 3 "Heavy voracious bunny buffalo"
        (b
          (z 6 6)
          #{(r 4 (p 2 5) :left)
            (d 5 (p 3 5))
            (d 6 (p 1 5))
            (d 7 (p 3 6))
            (d 8 (p 3 4))}))

   9 (s 9 "Gorilla gorilla gorilla"
        (b
          (z 30 30)
          #{(r 10 (p 3 3) :down)
            (d 11 (p 4 3))
            (d 12 (p 5 3))
            (d 13 (p 6 3))
            (d 14 (p 7 3))
            (d 15 (p 8 3))
            (d 16 (p 9 3))
            (d 17 (p 10 3))
            (d 18 (p 11 3))
            (d 19 (p 12 3))
            (d 20 (p 13 3))
            (d 21 (p 14 3))
            (d 22 (p 15 3))
            (d 23 (p 16 3))}))

   24 (s 24 "Flying furious rabbit"
         (b
           (z 43 32)
           #{(r 25 (p 3 3) :down)
             (d 26 (p 4 3))
             (d 27 (p 5 3))
             (d 28 (p 6 3))
             (d 29 (p 7 3))
             (d 30 (p 8 3))
             (d 31 (p 9 3))
             (d 32 (p 10 3))
             (d 33 (p 11 3))
             (d 34 (p 12 3))
             (d 35 (p 13 3))
             (d 36 (p 14 3))
             (d 37 (p 15 3))
             (d 38 (p 16 3))}))})

(defn get-default-data
  []
  {:simulations (get-default-simulations)
   :next-id     39})