(ns robots-vs-dinosaurs.logic.simulation
  (:require
    (robots-vs-dinosaurs.logic
      [scoreboard :as scoreboard]
      [board :as board]
      [size :as size]
      [unit :as unit]
      [point :as point])))

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

;;
;; Units
;;
(defn- update-unit
  [simulation unit-id f]
  (when-let [result (f (:board simulation) unit-id)]
    (if (board/message? result)
      result
      {:updated  (assoc simulation :board (:updated result))
       :response (:response result)})))

;;
;; Robots
;;
(defn add-robot
  [simulation robot]
  (board/add-robot (:board simulation) robot))

(defn get-robots
  [simulation]
  (board/get-robots (:board simulation)))

(defn get-robot
  [simulation robot-id]
  (board/get-robot (:board simulation) robot-id))

(defn move-robot-forward
  [simulation robot-id]
  (update-unit simulation robot-id board/move-robot-forward))

(defn move-robot-backward
  [simulation robot-id]
  (update-unit simulation robot-id board/move-robot-backward))

(defn turn-robot-left
  [simulation robot-id]
  (update-unit simulation robot-id board/turn-robot-left))

(defn turn-robot-right
  [simulation robot-id]
  (update-unit simulation robot-id board/turn-robot-right))

(defn robot-attack
  [simulation robot-id]
  (update-unit simulation robot-id board/robot-attack))

;;
;; Dinosaurs
;;
(defn add-dinosaur
  [simulation dinosaur]
  (board/add-dinosaur (:board simulation) dinosaur))

(defn get-dinosaurs
  [simulation]
  (board/get-dinosaurs (:board simulation)))

(defn get-dinosaur
  [simulation dinosaur-id]
  (board/get-dinosaur (:board simulation) dinosaur-id))

#_(def data
    (new-simulation
      0
      "chichila"
      (scoreboard/new-scoreboard)
      (board/new-board
        (size/new-size 10 10)
        #{(unit/new-robot
            0
            (point/new-point 0 0)
            :up)
          (unit/new-robot
            3
            (point/new-point 0 1)
            :up)
          (unit/new-dinosaur
            1
            (point/new-point 1 0))
          (unit/new-dinosaur
            2
            (point/new-point 3 3))})))