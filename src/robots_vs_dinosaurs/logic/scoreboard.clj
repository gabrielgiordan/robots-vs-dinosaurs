(ns robots-vs-dinosaurs.logic.scoreboard)

(defrecord Scoreboard [total])

(defn new-scoreboard
  ([n]
   (map->Scoreboard
     {:total n}))
  ([]
   (new-scoreboard 0)))

(defn total+
  [data n]
  (update-in data [:total] + n))