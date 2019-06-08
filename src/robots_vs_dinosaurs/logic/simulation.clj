(ns robots-vs-dinosaurs.logic.simulation
  (:require
    (robots-vs-dinosaurs.logic
      [scoreboard :as scoreboard]
      [board :as board])))

(defrecord Simulation [id title scoreboard board])

(defn new-simulation
  ([id title scoreboard board]
   (map->Simulation
     {:id         id
      :title      title
      :scoreboard scoreboard
      :board      board}))
  ([id title board]
   (new-simulation id title (scoreboard/new-scoreboard) board))
  ([id title]
   (new-simulation id title (board/new-board [50 50]))))

(defn reset
  [{:keys [id title board]}]
  (new-simulation id title (board/reset-board board)))

