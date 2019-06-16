(ns robots-vs-dinosaurs.storage.placeholder
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

(defonce
  placeholder
  {:simulations {0  (s 0 "Aerodynamic chinchilla"
                       (b
                         (z 15 15)
                         #{(r 1 (p 0 0) :up)
                           (d 2 (p 1 0) :mort)
                           (d 3 (p 1 2) :vita)
                           (d 4 (p 2 1) :doux)
                           (d 5 (p 4 3) :tard)
                           (d 6 (p 1 11) :vita)
                           (d 7 (p 12 14) :tard)
                           (d 8 (p 14 10) :tard)
                           (d 9 (p 1 14) :doux)
                           (d 10 (p 2 13) :vita)
                           (d 11 (p 14 6) :vita)
                           (d 12 (p 6 7) :mort)
                           (d 13 (p 9 9) :mort)
                           (d 14 (p 1 8) :vita)
                           (d 15 (p 1 10) :tard)}))

                 16 (s 16 "Heavy voracious bunny buffalo"
                       (b
                         (z 6 6)
                         #{(r 17 (p 2 5) :left)
                           (d 18 (p 3 5) :vita)
                           (d 19 (p 1 5) :mort)
                           (d 20 (p 3 6) :tard)
                           (r 21 (p 0 0) :up)
                           (d 22 (p 3 4) :tard)}))

                 23 (s 23 "Gorilla gorilla gorilla"
                       (b
                         (z 13 13)
                         #{(r 24 (p 3 3) :down)
                           (d 25 (p 4 2) :vita)
                           (d 26 (p 5 5) :vita)
                           (d 27 (p 6 3) :tard)
                           (d 28 (p 7 3) :mort)
                           (d 29 (p 8 12) :doux)
                           (d 30 (p 9 3) :doux)
                           (d 31 (p 10 3) :vita)
                           (d 32 (p 11 11) :vita)
                           (d 33 (p 12 3) :tard)
                           (d 34 (p 12 6) :doux)
                           (d 35 (p 14 3) :doux)
                           (d 36 (p 10 9) :doux)
                           (d 37 (p 12 1) :mort)}))

                 38 (s 38 "Flying furious rabbit"
                       (b
                         (z 17 17)
                         #{(r 39 (p 2 3) :down)
                           (d 40 (p 4 3) :vita)
                           (d 41 (p 5 3) :tard)
                           (d 42 (p 3 8) :mort)
                           (d 43 (p 7 3) :tard)
                           (d 44 (p 8 4) :tard)
                           (d 45 (p 9 3) :vita)
                           (d 46 (p 10 8) :vita)
                           (d 47 (p 11 3) :vita)
                           (d 48 (p 12 3) :vita)
                           (d 49 (p 13 0) :vita)
                           (d 50 (p 14 3) :vita)
                           (d 51 (p 15 15) :vita)
                           (d 52 (p 16 13) :vita)}))}
   :next-id     53})