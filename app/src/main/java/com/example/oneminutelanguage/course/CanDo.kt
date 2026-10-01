package com.example.oneminutelanguage.course

/** One "I can…" statement of the B1 checklist; [units] are the units that train it. */
class CanDo(val id: String, val skill: String, val text: String, val units: List<String>)

val CAN_DO_SKILLS = listOf("Reading", "Listening", "Speaking", "Writing")

val CAN_DO = listOf(
    CanDo("read1", "Reading", "I can understand everyday letters, emails and notices, for example from the municipality.", listOf("a2.t04", "b1.t04")),
    CanDo("read2", "Reading", "I can understand a job ad and workplace instructions such as safety rules or a rota.", listOf("b1.t01")),
    CanDo("read3", "Reading", "I can follow a news article on a familiar topic and find the main points.", listOf("b1.t04", "b1.t06")),
    CanDo("read4", "Reading", "I can find the key facts in a rental contract or a letter from an insurer.", listOf("b1.t03", "b1.t05")),
    CanDo("read5", "Reading", "I can read rules and forms about travel, such as delay compensation.", listOf("b1.t07")),
    CanDo("listen1", "Listening", "I can follow the main points of a news item in Makkelijke Taal.", listOf("b1.t04")),
    CanDo("listen2", "Listening", "I can understand clear, standard speech in routine conversations, for example with a teacher or a doctor.", listOf("b1.t02", "b1.t03")),
    CanDo("listen3", "Listening", "I can follow a short team meeting at work.", listOf("b1.t01")),
    CanDo("listen4", "Listening", "I can understand announcements, for example at a station.", listOf("a2.t03")),
    CanDo("listen5", "Listening", "I can follow a story about a trip told in the past tense.", listOf("b1.t07")),
    CanDo("speak1", "Speaking", "I can start and keep up a conversation on a familiar topic without preparing it.", listOf("b1.t06", "a2.t05")),
    CanDo("speak2", "Speaking", "I can describe an experience or a trip and say what I thought of it.", listOf("b1.t07")),
    CanDo("speak3", "Speaking", "I can answer common job-interview questions.", listOf("b1.t01")),
    CanDo("speak4", "Speaking", "I can describe symptoms to a doctor and say how long I have had them.", listOf("a2.t02", "b1.t03")),
    CanDo("speak5", "Speaking", "I can give my opinion with a reason and settle a small conflict politely.", listOf("b1.t05", "b1.t06")),
    CanDo("write1", "Writing", "I can write a simple connected text about a familiar topic.", listOf("b1.t07", "a2.t05")),
    CanDo("write2", "Writing", "I can write a personal message or a reply to an invitation.", listOf("a2.t05", "b1.t07")),
    CanDo("write3", "Writing", "I can write a formal email, for example an application.", listOf("b1.t01", "b1.t02")),
    CanDo("write4", "Writing", "I can write a complaint to a shop or the municipality.", listOf("b1.t04", "b1.t06")),
    CanDo("write5", "Writing", "I can write a short report of an incident at work.", listOf("b1.t03")),
)
