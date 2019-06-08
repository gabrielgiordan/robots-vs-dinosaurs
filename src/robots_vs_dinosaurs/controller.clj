(ns robots-vs-dinosaurs.controller
  (:require
    (robots-vs-dinosaurs.logic
      [simulation :as simulation]
      [board :as board]
      [unit :as unit])
    (robots-vs-dinosaurs.db
      [simulations :as db])))

;; --------
;; Simulations
;; --------
(defn get-id
  [{:keys [id]}]
  id)

(defn get-simulations
  "Lists simulations."
  [storage]
  (db/get-simulations storage))

(defn create-simulation!
  "Creates a simulation."
  [storage title size]
  (some->
    (db/get-id! storage)
    (as->
      $
      (some->>
        (board/new-board size)
        (simulation/new-simulation $ title)
        (db/create-simulation! storage)))))

(defn get-simulation
  "Gets a simulation."
  [storage simulation-id]
  (db/get-simulation storage simulation-id))

(defn delete-simulation!
  "Deletes a simulation."
  [storage simulation-id]
  (db/delete-simulation! storage simulation-id))

;; --------
;; Robots
;; --------
(defn create-robot!
  "Creates a robot inside of the simulation board."
  [storage simulation-id point orientation]
  (when-let
    [robot (some->
             (db/get-id! storage)
             (unit/new-robot point orientation))]
    (when
      (letfn
        [(add-robot
           [board]
           (board/add-unit board robot))]
        (db/update-simulation-in! storage simulation-id [:board] add-robot))
      robot)))

(defn get-robot
  "Gets the robot inside of the simulation board."
  [storage simulation-id robot-id]
  (some->
    (get-simulation storage simulation-id)
    (get :board)
    (board/get-unit robot-id)))

(defn get-robots
  "List the robots inside of the simulation board."
  [storage simulation-id]
  (some->
    (get-simulation storage simulation-id)
    (get :board)
    (board/get-robots)))

(defn- update-unit!
  ""
  [storage simulation-id unit-id f]
  (when-let [simulation (get-simulation storage simulation-id)]
    (when-let [updated-board (board/update-unit (:board simulation) unit-id f)]
      (when (->>
        (assoc simulation :board updated-board)
        (constantly)
        (db/update-simulation! storage simulation-id)))
      )))

(defn turn-robot-left!
  ""
  [storage simulation-id robot-id]
  (update-unit! storage simulation-id robot-id unit/turn-left))