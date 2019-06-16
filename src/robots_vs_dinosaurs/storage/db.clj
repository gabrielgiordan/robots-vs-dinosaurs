(ns robots-vs-dinosaurs.storage.db
  (:require [robots-vs-dinosaurs.component.storage :as storage]))

;;
;; Simulations
;;
(defn get-id!
  "Generates a next id and returns the current."
  [storage]
  (let [id (or (storage/pull storage :next-id) 0)]
    (storage/push! storage :next-id (inc id)) id))

(defn get-simulations
  "Gets all simulations of the storage."
  [storage]
  (or
    (some->
      (storage/pull storage :simulations)
      (vals))
    []))

(defn get-simulation
  "Gets a simulation from the storage."
  [storage id]
  (some->
    (storage/pull storage :simulations)
    (get id)))

(defn new-simulation!
  "Creates a simulation into the storage."
  [storage {:keys [id] :as simulation}]
  (when
    (some->
      (or (storage/pull storage :simulations) {})
      (assoc id simulation)
      (as-> $ (storage/push! storage :simulations $)))
    simulation))

(defn save-simulation!
  "Assoc a simulation using its `id`, returns `true` when success, else `false`."
  [storage simulation]
  (if
    (some->
      (storage/pull storage :simulations)
      (as-> $ (when (contains? $ (:id simulation)) $))
      (assoc (:id simulation) simulation)
      (as-> $ (storage/push! storage :simulations $)))
    true
    false))

(defn delete-simulation!
  "Returns `true` when deleted from storage with success, otherwise `false`."
  [storage id]
  (if
    (some->
      (storage/pull storage :simulations)
      (as-> $ (when (contains? $ id) $))
      (dissoc id)
      (as-> $ (storage/push! storage :simulations $)))
    true
    false))


