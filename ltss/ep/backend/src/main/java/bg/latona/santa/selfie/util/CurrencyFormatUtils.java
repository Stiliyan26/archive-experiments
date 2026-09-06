package bg.latona.santa.selfie.util;

import java.util.function.Function;


public class CurrencyFormatUtils {

    public static String convertNumbersToBgnCurrency(int number) {
        String[] units = {
                "нула", "един", "два", "три", "четири", "пет", "шест",
                "седем", "осем", "девет", "десет", "единадесет", "дванадесет"
        };

        String[] hundreds = {
                "", "сто", "двеста", "триста"
        };


        Function<Integer, Integer> divideBy = num -> number / num;
        Function<Integer, Integer> integerDivision = num -> number % num;

        int division_10 = divideBy.apply(10);
        int modularDivision_10 = integerDivision.apply(10);

        int division_100 = divideBy.apply(100);
        int modularDivision_100 = integerDivision.apply(100);

        int division_1_000 = divideBy.apply(1_000);
        int modular_division_1_000 = integerDivision.apply(1_000);

        int division_1_000_000 = divideBy.apply(1_000_000);
        int modular_division_1_000_000 = integerDivision.apply(1_000_000);

        int division_1_000_000_000 = divideBy.apply(1_000_000_000);
        int modular_division_1_000_000_000 = integerDivision.apply(1_000_000_000);


        if (number == 0)
            return units[0];

        if (number > 0 && number < 20)
            return number < 13 ? units[number] : units[modularDivision_10] + "надесет";

        if (number > 19 && number < 100) {
            String temp = units[division_10] + "десет";
            if (modularDivision_10 != 0)
                temp += " и " + convertNumbersToBgnCurrency(modularDivision_10);

            return temp;
        }

        if (number > 99 && number < 1_000) {
            String temp = division_100 < hundreds.length ? hundreds[division_100] : units[division_100] + "стотин";

            if ((modularDivision_100 % 10 == 0 || modularDivision_100 < 20) && modularDivision_100 != 0)
                temp += " и";

            if (modularDivision_100 != 0)
                temp += " " + convertNumbersToBgnCurrency(modularDivision_100);

            return temp;
        }

        if (number > 999 && number < 1_000_000) {
            String temp = (division_1_000 == 1) ? "хиляда" : ((division_1_000 == 2) ? "две хиляди" : convertNumbersToBgnCurrency(division_1_000) + " хиляди");

            if ((modular_division_1_000 % 10 == 0 || modular_division_1_000 < 20) && modular_division_1_000 != 0) {
                if (!((modularDivision_100 % 10 == 0 || modularDivision_100 < 20) && modularDivision_100 != 0))
                    temp += " и";
            }

            if ((modular_division_1_000 % 10 == 0 || modular_division_1_000 < 20) && modular_division_1_000 != 0 && modular_division_1_000 < 100)
                temp += " и";

            if (modular_division_1_000 != 0)
                temp += " " + convertNumbersToBgnCurrency(modular_division_1_000);

            return temp;
        }

        if (number > 999_999 && number < 1_000_000_000) {
            String tmp = (division_1_000_000 == 1) ? "един милион" : convertNumbersToBgnCurrency(division_1_000_000) + " милиона";

            if ((modular_division_1_000_000 % 10 == 0 || modular_division_1_000_000 < 20) && modular_division_1_000_000 != 0) {
                if (!((modular_division_1_000 % 10 == 0 || modular_division_1_000 < 20) && modular_division_1_000 != 0)) {
                    if (!((modularDivision_100 % 10 == 0 || modularDivision_100 < 20) && modularDivision_100 != 0))
                        tmp += " и";
                }
            }

            tmp += ", ";

            if ((modular_division_1_000_000 % 10 == 0 || modular_division_1_000_000 < 20) && modular_division_1_000_000 != 0 && modular_division_1_000_000 < 1000) {
                if ((modular_division_1_000 % 10 == 0 || modular_division_1_000 < 20) && modular_division_1_000 != 0 && modular_division_1_000 < 100)
                    tmp += " и";
            }

            if (modular_division_1_000_000 != 0)
                tmp += " " + convertNumbersToBgnCurrency(modular_division_1_000_000);

            return tmp;
        }

        if (number > 999_999_999 && number <= 2_000_000_000) {
            String tmp = (division_1_000_000_000 == 1) ? "един милиард" : "";

            tmp = (division_1_000_000_000 == 2) ? "два милиарда" : tmp;

            if (modular_division_1_000_000_000 != 0)
                tmp += " " + convertNumbersToBgnCurrency(modular_division_1_000_000_000);

            return tmp;
        }

        return "твърде голямо число";
    }
}
