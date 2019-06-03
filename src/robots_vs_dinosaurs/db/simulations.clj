(ns robots-vs-dinosaurs.db.simulations
  (:require [robots-vs-dinosaurs.component.storage :as storage]))

(defn next-id
  [storage]
  (let [id (or (storage/pull storage :identifier) 0)]
    (storage/push! storage :identifier (inc id))))

(defn simulations
  [storage]
  (or (storage/pull storage :simulations) {}))

(defn reset-simulations
  [storage]
  (storage/push! storage :simulations {}))

(defn add-simulation
  [storage simulation]
  (->> (assoc (simulations storage) (:id simulation) simulation)
       (storage/push! storage :simulations)))

(defn get-simulation
  [storage id]
  (get (simulations storage) id))
