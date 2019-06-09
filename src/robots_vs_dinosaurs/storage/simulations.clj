(ns robots-vs-dinosaurs.storage.simulations
  (:require [robots-vs-dinosaurs.component.storage :as storage]))

(def ^:private ^:const k :simulations)
(def ^:private ^:const i :identifiers)

(defn get-id!
  "Generates a next Id and gets the current."
  [storage]
  (let [id (or (storage/pull storage i) 0)]
    (storage/push! storage i (inc id)) id))

(defn get-simulations
  "Gets all simulations of the storage."
  [storage]
  (or
    (some->
      (storage/pull storage k)
      (vals))
    []))

(defn create-simulation!
  "Creates a simulation into the storage."
  [storage {:keys [id] :as simulation}]
  (when
    (some->
      (or (storage/pull storage k) {})
      (assoc id simulation)
      (as-> $ (storage/push! storage k $)))
    simulation))

(defn update-simulation!
  "Updates a simulation into the storage."
  [storage id f]
  (some->
    (storage/pull storage k)
    (update id f)
    (as->
      $
      (storage/push! storage k $)
      (get $ id))))

(defn update-simulation-in!
  "Updates-in a simulation into the storage."
  [storage id ks f]
  (when-some
    [simulations (storage/pull storage k)]
    (some->
      (get simulations id)
      (update-in ks f)
      (as->
        $
        (assoc simulations id $)
        (storage/push! storage k $)))))

(defn get-simulation
  "Gets a simulation from the storage."
  [storage id]
  (some->
    (storage/pull storage k)
    (get id)))

(defn delete-simulation!
  "Returns `true` when deleted from storage with success, otherwise `false`."
  [storage id]
  (if
    (some->
      (storage/pull storage k)
      (as-> $ (when (contains? $ id) $))
      (dissoc id)
      (as-> $ (storage/push! storage k $)))
    true
    false))