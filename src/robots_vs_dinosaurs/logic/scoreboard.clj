(ns robots-vs-dinosaurs.logic.scoreboard)



;(defprotocol IScoreboard
;  (add [this score])
;  (subtract [this score]))
;
;(defrecord Scoreboard [total] IScoreboard
;  (add
;    [_ score]
;    (->Scoreboard (+ total score)))
;  (subtract
;    [_ score]
;    (->Scoreboard (- total score))))
;
;(defn create-scoreboard
;  "Creates a new scoreboard."
;  []
;  (->Scoreboard 0))