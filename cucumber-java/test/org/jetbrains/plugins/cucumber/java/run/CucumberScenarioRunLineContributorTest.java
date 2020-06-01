// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import javax.swing.*;
import java.util.Date;

public class CucumberScenarioRunLineContributorTest extends CucumberJavaCodeInsightTestCase {


  private static final String myTestFeature = "Feature: My feature\n\n" +
                                              "Scenario: test\n" +
                                              "Given a cat";

  public void testScenarioRunLineContributorWhenScenarioNeverHaveBeenRan() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Run);
  }

  public void testScenarioRunLineContributorWhenScenarioHaveFailed() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":3";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.FAILED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Red2);
  }

  public void testScenarioRunLineContributorWhenScenarioHaveSucceeded() {
    PsiFile file = myFixture.configureByText("test.feature", myTestFeature);
    TestStateStorage stateStorage = TestStateStorage.getInstance(getProject());
    String testUrl = file.getVirtualFile().getUrl() + ":3";
    stateStorage.writeState(testUrl, new TestStateStorage.Record(TestStateInfo.Magnitude.PASSED_INDEX.getValue(), new Date(), 0, 0, "",
                                                                 "", ""));
    PsiElement element = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0);
    checkInfo(element, AllIcons.RunConfigurations.TestState.Green2);
  }
  
  public void testScenarioRunLineContributorForScenarioOutline() {
    String featureWithScenarioOutline = "Feature: myfeature\n\n" +
                                          "Scenario outline: <a>\n" +
                                            "Given: <a>\n" +
                                            "Examples:\n" +
                                              "| a |\n" +
                                              "| 1 |\n" +
                                              "| 2 |";
    PsiFile file = myFixture.configureByText("test.feature", featureWithScenarioOutline);
    checkInfo( ((GherkinFile)file).getFeatures()[0].getScenarios()[0].findElementAt(0), AllIcons.RunConfigurations.TestState.Run);
    PsiElement[] elements = ((GherkinFile)file).getFeatures()[0].getScenarios()[0].getLastChild().getChildren()[0].getChildren();
    checkInfo(elements[elements.length-2].findElementAt(0), AllIcons.RunConfigurations.TestState.Run);
    checkInfo(elements[elements.length-1].findElementAt(0), AllIcons.RunConfigurations.TestState.Run);

  }

  private static void checkInfo(PsiElement element, Icon run) {
    RunLineMarkerContributor.Info info = new ScenarioRunLineMarkerContributor().getInfo(element);
    assertNotNull(info);
    assertEquals(run, info.icon);
  }
}