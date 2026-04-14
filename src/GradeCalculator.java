
public class GradeCalculator {

    public static double calcPercentage(int obtained, int total) {
        if (total == 0) return 0;
        return ((double) obtained / total) * 100;
    }

    public static String getGrade(double percentage, boolean isAudit) {
        if (isAudit) return "P";

        if (percentage >= 90) return "O";
        else if (percentage >= 80) return "A+";
        else if (percentage >= 70) return "A";
        else if (percentage >= 60) return "B+";
        else if (percentage >= 50) return "B";
        else if (percentage >= 40) return "C";
        else return "F";
    }

    public static int getGradePoint(String grade) {
        return switch (grade) {
            case "O"  -> 10;
            case "A+" -> 9;
            case "A"  -> 8;
            case "B+" -> 7;
            case "B"  -> 6;
            case "C"  -> 5;
            case "P"  -> 1;
            default   -> 0;
        };
    }

    public static String getResult(String grade) {
        return grade.equals("F") ? "FAIL" : "PASS";
    }

    // SGPA = Σ(Credits × GradePoint) / Σ(Credits)
    public static double calcSGPA(int[] credits, int[] gradePoints) {
        int totalCredits = 0, weightedSum = 0;
        for (int i = 0; i < credits.length; i++) {
            if (credits[i] > 0) { // skip audit (0 credits)
                totalCredits += credits[i];
                weightedSum += credits[i] * gradePoints[i];
            }
        }
        if (totalCredits == 0) return 0;
        return Math.round((double) weightedSum / totalCredits * 100.0) / 100.0;
    }
}