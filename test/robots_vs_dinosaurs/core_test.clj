(ns robots-vs-dinosaurs.core-test
  (:require
    (clojure
      [test :refer :all])
    (clojure.spec
      [alpha :as spec])
    (robots-vs-dinosaurs.storage
      [placeholder :refer [placeholder]])
    (io.pedestal
      [http :as http]
      [test :refer :all])
    [clojure.edn :as edn]
    [robots-vs-dinosaurs.system :as system]
    [robots-vs-dinosaurs.logic.board :as board]
    [muuntaja.core :as muuntaja]
    [robots-vs-dinosaurs.adapter :as adapter])
  (:use
    (robots-vs-dinosaurs.logic
      [point]
      [direction]
      [size]
      [board])))

;; Aliases
(def ^:const p new-point)
(def ^:const d new-direction)
(def ^:const s new-size)
(def ^:const b new-board)

(def ^:const transit-json-fm "application/transit+json")
(def ^:const edn-fm "application/edn")
(def ^:const json-fm "application/json")

(def mocked-service
  (atom
    (->
      (get-in
        (system/mock-start-system :prod)
        [:router :router])
      (http/create-servlet)
      ::http/service-fn)))

(deftest integration-test

  (testing "Can get a simulation."
    (is (->
          @mocked-service
          (response-for
            :get "/api/simulations/0")
          (:body)
          (as->
            $
            (muuntaja/decode json-fm $)
            (every? $ [:id :board :scoreboard])))))

  (testing "Start with default data."
    (is (->
          @mocked-service
          (response-for
            :get "/api/simulations")
          (:body)
          (as->
            $
            (muuntaja/decode json-fm $))
          (count)
          (> 0))))

  (testing "Handle trailing slash `/` requests with redirect."
    (is (= (->
             @mocked-service
             (response-for
               :get "/api/simulations/")
             (:status))
           301)))

  (testing "Respond for `edn` requests with also 404 errors."
    (is (= (->
             @mocked-service
             (response-for
               :get "/98u321jka"
               :headers {"Accept" edn-fm})
             (:body)
             (edn/read-string)
             (:error)
             (:status))
           404)))

  (testing "Respond for `transit+json` requests with also 404 errors."
    (is (= (->
             @mocked-service
             (response-for
               :get "/98u321jka"
               :headers {"Accept" transit-json-fm})
             (:body)
             (as->
               $
               (muuntaja/decode transit-json-fm $))
             (:error)
             (:status))
           404))))

(deftest point--coercion

  (testing "A valid point is a two integer `x` and `y` point."
    (is (spec/valid? :point/point (p 5 5))))

  (testing "A string point it not a valid point."
    (is (not (spec/valid? :point/point (p "4" "2"))))))

(deftest point--arithmetic
  (testing "Can sum two points."
    (are [expected expr]
      (= expected expr)
      (p 7 8) (point+ (p 1 5) (p 6 3))
      (p 1 5) (point+ (p 0 3) (p 1 2))))

  (testing "Can subtract two points."
    (are [expected expr]
      (= expected expr)
      (p -5 2) (point- (p 1 5) (p 6 3))
      (p -1 1) (point- (p 0 3) (p 1 2))
      (p 10 3) (point- (p 15 10) (p 5 7)))))

(deftest point--arithmetic-collection

  (testing "Can sum a collection of points."
    (are [expected expr]
      (= expected expr)
      [(p 1 1) (p 2 2)] (points+ [(p 1 0) (p 2 1)] (p 0 1))
      [(p 10 22) (p 5 52) (p 32 26)] (points+ [(p 8 19) (p 3 49) (p 30 23)] (p 2 3))))

  (testing "Can subtract a collection of points, each point minus the given point."
    (are [expected expr]
      (= expected expr)
      [(p -1 -1) (p -2 -2)] (points- [(p 1 1) (p 0 0)] (p 2 2))
      [(p 6 16) (p 1 46) (p 28 20)] (points- [(p 8 19) (p 3 49) (p 30 23)] (p 2 3))))

  (testing "When sum or subtract an empty collection of points, return an empty collection."
    (is (= [] (points+ [] (p 4 3))))
    (is (= [] (points- [] (p 4 3))))))

(deftest point--direction

  (testing "Can get around directions of four sides:
            `up`, `right`, `down` and `left`."
    (is (= [(p 5 4) (p 6 5) (p 5 6) (p 4 5)]
           (around four-sides (p 5 5)))))

  (testing "Can move a point towards a direction of four or eight sides."
    (are [expected expr]
      (= expected expr)
      ;; Four sides
      (p 5 6) (toward (p 5 7) (d :up four-sides))
      (p 6 7) (toward (p 5 7) (d :right four-sides))
      (p 5 8) (toward (p 5 7) (d :down four-sides))
      (p 4 7) (toward (p 5 7) (d :left four-sides))))

  (testing "Can move a point away of a direction of four or eight sides."
    (are [expected expr]
      (= expected expr)
      (p 5 6) (away (p 5 7) (d :down four-sides))
      (p 6 7) (away (p 5 7) (d :left four-sides))
      (p 5 8) (away (p 5 7) (d :up four-sides))
      (p 4 7) (away (p 5 7) (d :right four-sides)))))

(deftest adapter-test

  (testing "Can convert a string to integer."

    (is (= (adapter/string->int "12345678")
           12345678))
    (is (= (adapter/string->orientation "up")
           :up))
    (is (= (adapter/string->orientation "down")
           :down))
    (is (= (adapter/string->orientation "right")
           :right))
    (is (= (adapter/string->orientation "left")
           :left))))

(deftest board-logic-test

  (testing "Out of bounds when lower than 0 and greater than size minus 1."
    (is (board/outbound? (b (s 20 20)) (p -1 0)))
    (is (board/outbound? (b (s 20 20)) (p 0 -1)))
    (is (board/outbound? (b (s 20 20)) (p 20 0)))
    (is (board/outbound? (b (s 20 20)) (p 0 20))))

  (testing "Inbound when zero or greater, or lower than size minus 1."
    (is (not (board/outbound? (b (s 20 20)) (p 1 0))))
    (is (not (board/outbound? (b (s 20 20)) (p 0 1))))
    (is (not (board/outbound? (b (s 20 20)) (p 19 0))))
    (is (not (board/outbound? (b (s 20 20)) (p 0 19))))))