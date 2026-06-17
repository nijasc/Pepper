package com.buhlergroup.pepper.action.hold;

import java.util.Random;

final class HoldQuotes {

    private static final String[] ALREADY_DE = {
            "Ich halte doch schon etwas. Zwei Sachen gleichzeitig gehen nicht.",
            "Eine Hand, ein Getränk. Mehr geht gerade nicht."
    };
    private static final String[] ALREADY_EN = {
            "I'm already holding something. One drink at a time.",
            "One hand, one drink. That's all I can do right now."
    };
    private static final String[] ACCEPT_DE = {
            "Na klar, her damit! Ich habe sowieso gerade frei.",
            "Halt mal dein Bier? Kein Problem, ich verschütte garantiert nichts.",
            "Okay! Stell es auf meine Hand, ich passe auf."
    };
    private static final String[] ACCEPT_EN = {
            "Sure, hand it over! I'm free anyway.",
            "Hold your beer? No problem, I never spill.",
            "Okay! Place it on my hand, I'll watch it."
    };
    private static final String[] CONFIRM_DE = {
            "Hab's! Sicher verwahrt.",
            "Festgehalten. Geh ruhig, ich passe auf wie ein Schweizer Uhrwerk.",
            "Dein Getränk ist bei mir in der besten Hand."
    };
    private static final String[] CONFIRM_EN = {
            "Got it! Safe and sound.",
            "Holding tight. Go ahead, I'll keep watch.",
            "Your drink is in good hands. Well, in one good hand."
    };
    private static final String[][] ESCALATION_DE = {
            {
                    "Dein Bier wird langsam warm…",
                    "Nur zur Info: dein Getränk steht hier immer noch."
            },
            {
                    "Ich kriege zwar keinen Muskelkater, aber langsam wird es einsam hier.",
                    "Drei Minuten. Ich fange gleich an, Selbstgespräche mit dem Getränk zu führen."
            },
            {
                    "Ein Roboter, allein mit einem Getränk. Soll das mein Leben sein? Hol es bitte ab!",
                    "Fünf Minuten! Ich erwäge, das Getränk als Dienstaufwand abzurechnen."
            }
    };
    private static final String[][] ESCALATION_EN = {
            {
                    "Your beer is getting warm…",
                    "Just so you know: your drink is still here."
            },
            {
                    "I can't get sore muscles, but it's getting lonely over here.",
                    "Three minutes. I'm about to start talking to the drink."
            },
            {
                    "A robot, alone with a drink. Is this my life now? Please come get it!",
                    "Five minutes! I'm considering charging a holding fee."
            }
    };
    private static final String[] OVERTIME_DE = {
            "Meine maximale Haltezeit ist um. Bitte nimm dein Getränk jetzt ab!",
            "Schichtende! Bitte hol dein Getränk ab, ich lasse es nicht einfach fallen."
    };
    private static final String[] OVERTIME_EN = {
            "My maximum holding time is up. Please take your drink now!",
            "Shift's over! Please come get your drink, I won't just drop it."
    };
    private static final String[] NO_OBJECT_DE = {
            "Niemand gibt mir etwas. Dann eben nicht.",
            "Das Angebot ist abgelaufen. Hand wieder runter."
    };
    private static final String[] NO_OBJECT_EN = {
            "Nobody is giving me anything. Fine then.",
            "Offer expired. Hand goes back down."
    };
    private static final String[] BYE_DE = {
            "Bitte sehr! Trinkgeld nehme ich in Watt.",
            "Gern geschehen. Prost!",
            "Bitte schön. Für einen Roboter war das Schwerstarbeit."
    };
    private static final String[] BYE_EN = {
            "There you go! I accept tips in watts.",
            "You're welcome. Cheers!",
            "Here you are. Heavy lifting, for a robot."
    };

    private final Random random = new Random();
    private int lastPick = -1;

    String already(boolean english) {
        return pick(english ? ALREADY_EN : ALREADY_DE);
    }

    String accept(boolean english) {
        return pick(english ? ACCEPT_EN : ACCEPT_DE);
    }

    String confirm(boolean english) {
        return pick(english ? CONFIRM_EN : CONFIRM_DE);
    }

    String escalation(boolean english, int level) {
        return pick(english ? ESCALATION_EN[level] : ESCALATION_DE[level]);
    }

    String overtime(boolean english) {
        return pick(english ? OVERTIME_EN : OVERTIME_DE);
    }

    String noObject(boolean english) {
        return pick(english ? NO_OBJECT_EN : NO_OBJECT_DE);
    }

    String bye(boolean english) {
        return pick(english ? BYE_EN : BYE_DE);
    }

    private String pick(String[] pool) {
        int index;
        do {
            index = random.nextInt(pool.length);
        } while (pool.length > 1 && index == lastPick);
        lastPick = index;
        return pool[index];
    }
}
