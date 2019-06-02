(ns robots-vs-dinosaurs.logic.simulation
  (:require [robots-vs-dinosaurs.logic.scoreboard :as scoreboard]
            [robots-vs-dinosaurs.logic.board :as board]))

(defrecord Simulation [id scoreboard board])

(defn new-simulation
  ([id scoreboard board]
   (->Simulation id scoreboard board))
  ([id board]
   (new-simulation id (scoreboard/new-scoreboard) board)))

(defn reset
  [{:keys [id board]}]
  (new-simulation id (board/reset-board board)))