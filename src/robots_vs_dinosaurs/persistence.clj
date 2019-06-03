(ns robots-vs-dinosaurs.persistence
  (:require [robots-vs-dinosaurs.component.storage :as storage]
            [robots-vs-dinosaurs.logic.simulation :as simulation]
            [robots-vs-dinosaurs.util :as u]))

(defn all-simulations
  [storage]
  (if-some [simulations (storage/pull storage :simulations)]
    simulations
    #{}))

(defn assoc-simulations!
  [storage simulations]
  (:simulations (storage/push! storage :simulations simulations)))

(defn find-simulation
  [simulations id]
  (u/find-first simulations [:id id]))

(defn add-simulation!
  ([storage simulations simulation]
   (assoc-simulations! storage (conj simulations simulation)))
  ([storage simulation]
   (add-simulation! storage (all-simulations storage) simulation)))

(defn get-simulation
  [storage id]
  (find-simulation (all-simulations storage) id))

(defn update-simulation
  [storage id update-function]
  (let [simulations (all-simulations storage)
        simulation (find-simulation simulations id)
        updated (update-function simulation)]
  (assoc-simulations! storage (conj (updated disj simulations simulation)))))

(defn delete-simulation!
  ([storage simulations simulation]
   (assoc-simulations! storage (disj simulations simulation)))
  ([storage simulation]
   (delete-simulation! storage (all-simulations storage) simulation)))

(defn delete-simulation-by-id!
  [storage id]
  (let [simulations (all-simulations storage)
        simulation (find-simulation simulations id)]
    (delete-simulation! storage simulations simulation)))

(defn all-simulation-ids
  [storage]
  (reduce conj #{} (flatten (map :id (all-simulations storage)))))