(ns robots-vs-dinosaurs.controller
  (:require
    (robots-vs-dinosaurs.logic
      [simulation :as simulation]
      [board :as board]
      [unit :as unit])
    (robots-vs-dinosaurs.storage
      [db :as db])))

(defn- result-handler
  [result]
  (if (keyword? result)
    (throw
      (ex-info "Invalid operation." {:type result})))
  result)

;;
;; Simulations
;;
(defn get-simulations
  "Gets all stored simulations."
  [storage]
  (db/get-simulations storage))

(defn get-simulation
  "Gets a stored simulation."
  [storage simulation-id]
  (db/get-simulation storage simulation-id))

(defn delete-simulation!
  "Deletes the stored simulation."
  [storage simulation-id]
  (db/delete-simulation! storage simulation-id))

(defn new-simulation!
  "Creates and stores a new simulation."
  [storage title size]
  (some->>
    (board/new-board size)
    (simulation/new-simulation (db/get-id! storage) title)
    (db/new-simulation! storage)))

(defn- update-simulation!
  "Updates simulation and stores the simulation."
  [storage simulation-id simulation-handler]
  (when-some
    [{:keys [updated response]}
     (some->
       (db/get-simulation storage simulation-id)
       (simulation-handler)
       (result-handler))]
    (when
      (db/save-simulation! storage updated)
      response)))

(defn get-simulation-as-game
  [storage simulation-id]
  (some->
    (db/get-simulation storage simulation-id)
    (simulation/as-game)))

;;
;; Robots
;;
(defn new-robot!
  "Creates and stores a new robot into the simulation."
  [storage simulation-id point orientation]
  (let [robot (unit/new-robot (db/get-id! storage) point orientation)]
    (update-simulation! storage simulation-id #(simulation/add-robot % robot))))

(defn get-robot
  "Gets the robot inside the simulation board."
  [storage simulation-id robot-id]
  (some->
    (get-simulation storage simulation-id)
    (:board)
    (board/get-robot robot-id)
    (result-handler)))

(defn get-robots
  "List the robots inside the simulation board."
  [storage simulation-id]
  (some->
    (get-simulation storage simulation-id)
    (:board)
    (board/get-robots)
    (result-handler)))

(defn turn-robot-left!
  "Turns and stores the robot left inside the simulation board."
  [storage simulation-id robot-id]
  (update-simulation! storage simulation-id #(simulation/turn-robot-left % robot-id)))

(defn turn-robot-right!
  "Turns and stores the robot right inside the simulation board."
  [storage simulation-id robot-id]
  (update-simulation! storage simulation-id #(simulation/turn-robot-right % robot-id)))

(defn move-robot-forward!
  "Moves forward and stores the robot inside the simulation board."
  [storage simulation-id robot-id]
  (update-simulation! storage simulation-id #(simulation/move-robot-forward % robot-id)))

(defn move-robot-backward!
  "Moves backward and stores the robot inside the simulation board."
  [storage simulation-id robot-id]
  (update-simulation! storage simulation-id #(simulation/move-robot-backward % robot-id)))

(defn robot-attack!
  "Make the robot attack and removes the attacked units inside the simulation board."
  [storage simulation-id robot-id]
  (update-simulation! storage simulation-id #(simulation/robot-attack % robot-id)))

;;
;; Dinosaurs
;;
(defn new-dinosaur!
  "Creates and stores a new dinosaur into the simulation."
  [storage simulation-id point subtype]
  (let [dinosaur (unit/new-dinosaur (db/get-id! storage) point subtype)]
    (update-simulation! storage simulation-id #(simulation/add-dinosaur % dinosaur))))

(defn get-dinosaur
  "Gets the dinosaur inside of the simulation board."
  [storage simulation-id dinosaur-id]
  (some->
    (get-simulation storage simulation-id)
    (:board)
    (board/get-dinosaur dinosaur-id)
    (result-handler)))

(defn get-dinosaurs
  "List the dinosaurs inside of the simulation board."
  [storage simulation-id]
  (some->
    (get-simulation storage simulation-id)
    (:board)
    (board/get-dinosaurs)
    (result-handler)))