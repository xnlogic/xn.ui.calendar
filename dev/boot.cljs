(ns dev.boot
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-time.core :as time]
            [xn.ui.calendar :as cal]
            [xn.library.popup :as p]
            [figwheel.client :as fw]))

(enable-console-print!)
(fw/watch-and-reload)

(defn show-cal [cur owner]
  (om/component
    (dom/div
      nil
      (cal/calendar {:active-date (:date cur)}))))

(defn show-cal-control [cur owner]
  (om/component
    (dom/div
      nil
      (om/build cal/calendar-component cur {:opts {:key :date}}))))

(defn show-clock [cur owner]
  (om/component
    (dom/div
      nil
      (cal/clock (:date cur) false 1 nil))))

(defn show-datetime-control [cur owner]
  (om/component
    (dom/div
      nil
      (om/build cal/datetime-component cur {:opts {:key :date}}))))

(defn show-date-range-control [cur owner]
  (om/component
    (dom/div
      nil
      (om/build cal/date-range-component cur {:opts {:start :date :end :end-date :time? true}}))))

(p/init-popups (js/document.getElementById "popups"))

(om/root show-date-range-control
         {:date (time/now)
          :end-date (time/now)}
         {:target (js/document.getElementById "contents")})
