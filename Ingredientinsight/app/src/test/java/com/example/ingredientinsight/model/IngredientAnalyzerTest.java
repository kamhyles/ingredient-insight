package com.example.ingredientinsight.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests the educational analyzer's public behavior and documents its limitations. */
public class IngredientAnalyzerTest {

    @Test
    public void nullInputReturnsUnknownWithoutMatches() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze(null);

        assertEquals(0, result.healthScore);
        assertEquals("Unknown", result.healthLabel);
        assertTrue(result.flaggedIngredients.isEmpty());
        assertTrue(result.explanation.contains("No ingredients provided"));
    }

    @Test
    public void blankInputReturnsUnknown() {
        assertEquals("Unknown", IngredientAnalyzer.analyze(" \n\t ").healthLabel);
    }

    @Test
    public void unrecognizedIngredientsReturnModerationWithExplanation() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("water, salt");

        assertEquals(0, result.healthScore);
        assertEquals("Moderation", result.healthLabel);
        assertTrue(result.flaggedIngredients.isEmpty());
        assertTrue(result.explanation.contains("No specific red flags"));
    }

    @Test
    public void scoreOfFiveIsBetterChoice() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("quinoa, almonds");

        assertEquals(5, result.healthScore);
        assertEquals("Better choice", result.healthLabel);
        assertTrue(result.explanation.contains("Overall score: +5"));
    }

    @Test
    public void scoreOfFourRemainsModeration() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("brown rice, almonds");

        assertEquals(4, result.healthScore);
        assertEquals("Moderation", result.healthLabel);
    }

    @Test
    public void scoreOfMinusFiveIsLessHealthy() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("sugar, canola oil");

        assertEquals(-5, result.healthScore);
        assertEquals("Less healthy", result.healthLabel);
    }

    @Test
    public void scoreOfMinusFourRemainsModeration() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("dextrose, canola oil");

        assertEquals(-4, result.healthScore);
        assertEquals("Moderation", result.healthLabel);
    }

    @Test
    public void positiveAndNegativeIngredientsContributeToScore() {
        IngredientAnalyzer.AnalysisResult result =
                IngredientAnalyzer.analyze("quinoa, olive oil, sugar");

        assertEquals(3, result.healthScore);
        assertEquals("Moderation", result.healthLabel);
        assertEquals(3, result.flaggedIngredients.size());
    }

    @Test
    public void matchingIgnoresCaseAndSurroundingWhitespace() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("  SuGaR , OLIVE OIL ");

        assertEquals(0, result.healthScore);
        assertEquals("sugar", result.flaggedIngredients.get(0));
        assertEquals("olive oil", result.flaggedIngredients.get(1));
    }

    @Test
    public void wordBoundariesAvoidMatchingEggInsideEggplant() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("eggplant");

        assertEquals(0, result.healthScore);
        assertTrue(result.flaggedIngredients.isEmpty());
    }

    @Test
    public void semicolonsAndParenthesesSeparateIngredients() {
        IngredientAnalyzer.AnalysisResult result =
                IngredientAnalyzer.analyze("quinoa; dressing (olive oil, sugar)");

        assertEquals(3, result.healthScore);
        assertEquals(3, result.flaggedIngredients.size());
    }

    @Test
    public void emptyIngredientSegmentsAreIgnored() {
        assertEquals(3, IngredientAnalyzer.analyze(",;() quinoa,,, ").healthScore);
    }

    @Test
    public void explanationCategoriesHaveStableAlphabeticalOrder() {
        String explanation = IngredientAnalyzer.analyze("quinoa, sugar, olive oil").explanation;

        assertTrue(explanation.indexOf("Added sugar:") < explanation.indexOf("Healthy fats:"));
        assertTrue(explanation.indexOf("Healthy fats:") < explanation.indexOf("Whole grains:"));
    }

    @Test
    public void repeatedIngredientsCountAgainButOnlyProduceOneMessage() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("sugar, sugar");

        // Current limitation: scoring counts occurrences rather than unique ingredients.
        assertEquals(-6, result.healthScore);
        assertEquals(1, result.flaggedIngredients.size());
        assertEquals(result.explanation.indexOf("Contains added sugar"),
                result.explanation.lastIndexOf("Contains added sugar"));
    }

    @Test
    public void onlyFirstMatchingRuleContributesForEachIngredientSegment() {
        IngredientAnalyzer.AnalysisResult result = IngredientAnalyzer.analyze("whole oats");

        assertEquals(3, result.healthScore);
        assertEquals(1, result.flaggedIngredients.size());
        assertTrue(result.explanation.contains("Contains whole oats"));
        assertFalse(result.explanation.contains("Contains oats\n"));
    }

    @Test
    public void overlappingRulesCurrentlyDependOnDeclarationOrder() {
        // The refined flour rule precedes the whole grain rule. This test records
        // existing behavior so a future specificity fix can change it deliberately.
        IngredientAnalyzer.AnalysisResult result =
                IngredientAnalyzer.analyze("whole grain wheat flour");

        assertEquals(-2, result.healthScore);
        assertTrue(result.explanation.contains("Contains refined wheat flour"));
    }
}
