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
          (z 15 15)
          #{(r 1 (p 0 0) :up)
            (d 2 (p 1 0))
            (d 3 (p 1 2))
            (d 4 (p 2 1))
            (d 5 (p 4 3))
            (d 6 (p 1 11))
            (d 7 (p 12 14))
            (d 8 (p 14 10))
            (d 9 (p 1 14))
            (d 10 (p 2 13))
            (d 11 (p 14 6))
            (d 12 (p 6 7))
            (d 13 (p 9 9))
            (d 14 (p 1 8))
            (d 15 (p 1 10))}))

   16 (s 16 "Heavy voracious bunny buffalo"
        (b
          (z 6 6)
          #{(r 17 (p 2 5) :left)
            (d 18 (p 3 5))
            (d 19 (p 1 5))
            (d 20 (p 3 6))
            (r 21 (p 0 0) :up)
            (d 22 (p 3 4))}))

   23 (s 23 "Gorilla gorilla gorilla"
        (b
          (z 13 13)
          #{(r 24 (p 3 3) :down)
            (d 25 (p 4 2))
            (d 26 (p 5 5))
            (d 27 (p 6 3))
            (d 28 (p 7 3))
            (d 29 (p 8 12))
            (d 30 (p 9 3))
            (d 31 (p 10 3))
            (d 32 (p 11 11))
            (d 33 (p 12 3))
            (d 34 (p 12 6))
            (d 35 (p 14 3))
            (d 36 (p 10 9))
            (d 37 (p 12 1))}))

   38 (s 38 "Flying furious rabbit"
         (b
           (z 17 17)
           #{(r 39 (p 2 3) :down)
             (d 40 (p 4 3))
             (d 41 (p 5 3))
             (d 42 (p 3 8))
             (d 43 (p 7 3))
             (d 44 (p 8 4))
             (d 45 (p 9 3))
             (d 46 (p 10 8))
             (d 47 (p 11 3))
             (d 48 (p 12 3))
             (d 49 (p 13 0))
             (d 50 (p 14 3))
             (d 51 (p 15 15))
             (d 52 (p 16 13))}))})

(defn get-default-data
  []
  {:simulations (get-default-simulations)
   :next-id     53})