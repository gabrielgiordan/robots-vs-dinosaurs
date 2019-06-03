(ns robots-vs-dinosaurs.controller
  (:require [robots-vs-dinosaurs.logic.simulation :as simulation]
            [robots-vs-dinosaurs.logic.board :as board]
            [robots-vs-dinosaurs.db.simulations :as db.simulations]))

(defn simulations
  [storage]
  (or (vals (db.simulations/simulations storage)) {}))

(defn reset-simulations
  [storage]
  (db.simulations/reset-simulations storage))

(defn new-simulation
  ([storage title size]
  (let [id (db.simulations/next-id storage)
        board (board/new-board size)
        simulation (simulation/new-simulation id title board)]
    (db.simulations/add-simulation storage simulation)
    simulation)))

(defn get-simulation
  [storage id]
  (db.simulations/get-simulation storage id))