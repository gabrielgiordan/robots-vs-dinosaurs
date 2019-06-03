(ns robots-vs-dinosaurs.logic.board
  (:require [robots-vs-dinosaurs.util :as u]))

(defrecord Board [size units])

(defn new-board
  ([size units]
   (map->Board
     {:size size
      :units units}))
  ([size]
   (new-board size #{})))

(defn reset-board
  [{:keys [size]}]
  (new-board size))

(defn add-unit
  [{:keys [units] :as board} unit]
  (assoc-in board [:units] (conj units unit)))

(defn remove-unit
  [{:keys [units] :as board} unit]
  (assoc-in board [:units] (disj units unit)))

(defn find-unit
  [{:keys [units]} id]
  (u/find-first units [:id id]))

(defn find-unit-at
  [{:keys [units]} point]
  (u/find-first units [:point point]))

(defn find-units-at
  [{:keys [units]} coll]
  [(doseq [point coll]
    (when-some [unit (find-unit-at units point)]
      unit))])

(defn update-unit
  [{:keys [units] :as board} unit func]
  (if-let [updated (func unit)]
    (let [updated-units (-> units (disj unit) (conj updated))]
      (update-in board [:units] updated-units))))