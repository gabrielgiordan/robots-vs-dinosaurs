(ns robots-vs-dinosaurs.logic.board
  (:require
    (robots-vs-dinosaurs
      [util :refer [find-first]])
    (robots-vs-dinosaurs.logic
      [unit :as unit])))

(defrecord Board [size units])

(defn new-board
  ([size units]
   (map->Board
     {:size  size
      :units units}))
  ([size]
   (new-board size #{})))

(defn reset-board
  [{:keys [size]}]
  (new-board size))

(defn add-unit
  [{:keys [units] :as board} unit]
  (assoc-in board [:units] (conj units unit)))

(defn delete-unit
  [{:keys [units] :as board} unit]
  (assoc-in board [:units] (disj units unit)))

(defn get-robots
  [{:keys [units]}]
  (filter unit/robot? units))

(defn get-dinosaurs
  [{:keys [units]}]
  (filter unit/dinosaur? units))

(defn get-unit
  [{:keys [units]} id]
  (find-first units [:id id]))

(defn get-unit-at
  [{:keys [units]} point]
  (find-first units [:point point]))

(defn get-units-at
  [{:keys [units]} coll]
  [(doseq [point coll]
     (when-some [unit (get-unit-at units point)]
       unit))])

(defn update-unit
  [{:keys [units] :as board} id f]
  (some->
    (get-unit board id)
    (as->
      $
      (assoc-in board [:units] (conj (disj units $) (f $))))))