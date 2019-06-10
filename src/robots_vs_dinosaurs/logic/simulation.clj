(ns robots-vs-dinosaurs.logic.simulation
  (:require
    (robots-vs-dinosaurs.logic
      [scoreboard :as scoreboard]
      [board :as board]
      [size :as size]
      [unit :as unit]
      [point :as point])))

;;
;; Simulation
;;
(defrecord Simulation [id title scoreboard board])

(defn new-simulation
  "Creates a new simulation."
  ([id title scoreboard board]
   (map->Simulation
     {:id         id
      :title      title
      :scoreboard scoreboard
      :board      board}))
  ([id title board]
   (new-simulation id title (scoreboard/new-scoreboard) board))
  ([id title]
   (new-simulation id title (board/new-board))))

;;
;; Board
;;
(defn- update-board
  "Updates the simulation board, returns an `:updated`
  simulation and a `:response`, else a keyword message."
  [simulation result]
  (if (board/message? result)
    result
    {:updated  (assoc simulation :board (:updated result))
     :response (:response result)}))

;;
;; Robots
;;
(defn add-robot
  "Add a robot."
  [{:keys [board] :as simulation} robot]
  (some->>
    (board/add-robot board robot)
    (update-board simulation)))

(defn get-robots
  "Gets all robots."
  [{:keys [board]}]
  (board/get-robots board))

(defn get-robot
  "Gets a robot."
  [{:keys [board]} robot-id]
  (board/get-robot board robot-id))

(defn move-robot-forward
  "Makes a robot move forward."
  [{:keys [board] :as simulation} robot-id]
  (some->>
    (board/move-robot-forward board robot-id)
    (update-board simulation)))

(defn move-robot-backward
  "Makes a robot move backward."
  [{:keys [board] :as simulation} robot-id]
  (some->>
    (board/move-robot-backward board robot-id)
    (update-board simulation)))

(defn turn-robot-left
  "Makes a robot turn left."
  [{:keys [board] :as simulation} robot-id]
  (some->>
    (board/turn-robot-left board robot-id)
    (update-board simulation)))

(defn turn-robot-right
  "Makes a robot turn right."
  [{:keys [board] :as simulation} robot-id]
  (some->>
    (board/turn-robot-right board robot-id)
    (update-board simulation)))

(defn robot-attack
  "Makes a robot attack, then add the result to the scoreboard."
  [{:keys [board] :as simulation} robot-id]
  (when-let
    [{:keys [response] :as result} (some->>
                                     (board/robot-attack board robot-id)
                                     (update-board simulation))]
    (if response
      (update-in result [:updated :scoreboard] #(scoreboard/total* % (count response)))
      result)))

;;
;; Dinosaurs
;;
(defn add-dinosaur
  "Adds a dinosaur."
  [{:keys [board] :as simulation} dinosaur]
  (some->>
    (board/add-dinosaur board dinosaur)
    (update-board simulation)))

(defn get-dinosaurs
  "Gets all dinosaurs."
  [{:keys [board]}]
  (board/get-dinosaurs board))

(defn get-dinosaur
  "Gets a dinosaur."
  [{:keys [board]} dinosaur-id]
  (board/get-dinosaur board dinosaur-id))

;;
;; Matrix
;;
(defn- as-matrix
  "Converts a simulation to matrix."
  [{{{:keys [width height]} :size :as board} :board}]
  (->>
    (repeat height (repeat width "_"))
    (map-indexed
      (fn [y coll]
        (vec
          (map-indexed
            (fn [x item]
              (let [unit (board/get-unit-at board (point/new-point x y))]
                (cond
                  (unit/robot? unit) (str "R")
                  (unit/dinosaur? unit) (str "D")
                  :else item)))
            coll))))
    (vec)))

(defn as-game
  "Converts a simulation to a simple game string."
  [{{:keys [total]} :scoreboard :as simulation}]
  (str
    "Robots vs Dinosaurs\n"
    "\nScore: " total "\n\n"
    (->>
      (map
        (partial clojure.string/join "|")
        (as-matrix simulation))
      (clojure.string/join "\n"))))

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