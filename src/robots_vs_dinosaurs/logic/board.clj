(ns robots-vs-dinosaurs.logic.board
  (:require [robots-vs-dinosaurs.util :as u]))

(defrecord Board [size units])

(defn new-board
  ([size units]
   (->Board size units))
  ([size]
   (->Board size #{})))

(defn reset-board
  [{:keys [size]}]
  (new-board size))

(defn add-unit
  [data unit]
  (assoc-in data [:units] (conj (:units data) unit)))

(defn remove-unit
  [data unit]
  (assoc-in data [:units] (disj (:units data) unit)))

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
  [data unit func]
  (if-let [new (func unit)]
    (let [units (-> (:units data)
                    (disj unit)
                    (conj new))]
      (update-in data [:units] units))))