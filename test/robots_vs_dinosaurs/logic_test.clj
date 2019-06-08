(ns robots-vs-dinosaurs.logic-test
  (:require
    (clojure
      [test :refer :all])
    (clojure.spec
      [alpha :as spec]))
  (:use
    (robots-vs-dinosaurs.logic
      [point]
      [direction]
      [size])))

;; Aliases
(def ^:const p new-point)
(def ^:const d new-direction)
(def ^:const s new-size)

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
    (is (= [(p 5 6) (p 6 5) (p 5 4) (p 4 5)]
           (around four-sides (p 5 5)))))

  (testing "Can get around directions of eight sides:
            `up-left`, `up`, `up-right`, `right`, `down-right`, `down`, `down-left`, `left`"
    (is (= [(p 4 6) (p 5 6) (p 6 6) (p 6 5) (p 6 4) (p 5 4) (p 4 4) (p 4 5)]
           (around eight-sides (p 5 5)))))

  (testing "Can move a point towards a direction of four or eight sides."
    (are [expected expr]
      (= expected expr)
      ;; Four sides
      (p 5 8) (toward (p 5 7) (d :up four-sides))
      (p 6 7) (toward (p 5 7) (d :right four-sides))
      (p 5 6) (toward (p 5 7) (d :down four-sides))
      (p 4 7) (toward (p 5 7) (d :left four-sides))
      ;; Eight sides
      (p 4 8) (toward (p 5 7) (d :up-left eight-sides))
      (p 6 8) (toward (p 5 7) (d :up-right eight-sides))
      (p 4 6) (toward (p 5 7) (d :down-left eight-sides))
      (p 6 6) (toward (p 5 7) (d :down-right eight-sides))))

  (testing "Can move a point away of a direction of four or eight sides."
    (are [expected expr]
      (= expected expr)
      ;; Four sides
      (p 5 8) (away (p 5 7) (d :down four-sides))
      (p 6 7) (away (p 5 7) (d :left four-sides))
      (p 5 6) (away (p 5 7) (d :up four-sides))
      (p 4 7) (away (p 5 7) (d :right four-sides))
      ;; Eight sides
      (p 4 8) (away (p 5 7) (d :down-right eight-sides))
      (p 6 8) (away (p 5 7) (d :down-left eight-sides))
      (p 4 6) (away (p 5 7) (d :up-right eight-sides))
      (p 6 6) (away (p 5 7) (d :up-left eight-sides)))))

(deftest point--validation

  (testing "Out of bounds when lower than 0 and greater than size minus 1."
    (is (not (inbound? (p -1 0) (s 20 20))))
    (is (not (inbound? (p 0 -1) (s 20 20))))
    (is (not (inbound? (p 20 0) (s 20 20))))
    (is (not (inbound? (p 0 20) (s 20 20)))))

  (testing "Inbound when zero or greater, or lower than size minus 1."
    (is (inbound? (p 1 0) (s 20 20)))
    (is (inbound? (p 0 1) (s 20 20)))
    (is (inbound? (p 19 0) (s 20 20)))
    (is (inbound? (p 0 19) (s 20 20)))))