/**
 * Copyright 2022 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.cp3.core.jasmin.integration;

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class CompleteAppsJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/integration/";


    public CompleteAppsJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void HelloWorld() {
        var res = toJasmin("HelloWorld");
        executeExpecting(res.jasmin(),
                "Hello, World!");
    }

    @Test
    public void Simple() {
        var res = toJasmin("Simple");
        executeExpecting(res.jasmin(),
                "30");
    }

    @Test
    public void FindMaximum() {
        var res = toJasmin("FindMaximum");
        executeExpecting(res.jasmin(),
                "Result: 28");
    }

    @Test
    public void WhileAndIfChained() {
        var expected = List.of(0, -6, -4, -2, 0, 2, 4, 6, 8, 10);
        var res = toJasmin("WhileAndIfChained");
        executeExpecting(res.jasmin(), expected.stream().map(i -> "" + i).collect(Collectors.joining("\n")));
    }

    @Test
    public void WhileAndIfSeq() {
        var expected = List.of(0, -6, -4, -2, 0, 2, 4, 6, 8, 10);
        var res = toJasmin("WhileAndIfSeq");
        executeExpecting(res.jasmin(), expected.stream().map(i -> "" + i).collect(Collectors.joining("\n")));
    }

    @Test
    public void QuickSort() {
        var expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        var res = toJasmin("QuickSort");
        executeExpecting(res.jasmin(), expected.stream().map(i -> "" + i).collect(Collectors.joining("\n")));
    }

    @Test
    public void Lazysort() {
        var expected = List.of(6, 6, 5, 8, 2, 11, 6, 9, 10, 4);
        var res = toJasmin("Lazysort", Quicksort.class);
        executeExpecting(res.jasmin(), expected.stream().map(i -> "" + i).collect(Collectors.joining("\n")));
    }

    @Test
    public void Life() {
        var res = toJasmin("Life");
        var numResults = 3; //at most 50
        var expectedLife = SpecsStrings.normalizeFileContents(SpecsIo.read(RESOURCES_LOCATION + "/" + BASE_PATH + "/expected/Life.log").trim(), true);
        var input = "1\n".repeat(numResults - 1) + "0\n";
        var ret = executeWith(res.jasmin(), input);
        var output = SpecsStrings.normalizeFileContents(ret.getFullOutput().trim(), true);
        assertTrue("Life for %d iterations should be within expected life".formatted(numResults), expectedLife.contains(output),
                expectedLife, output);
    }

    @Test
    public void MonteCarloPi() {
        var res = toJasmin("MonteCarloPi");
        var input = "10000\n";
        var output = "Insert number: Result: 312";
        execute(res.jasmin(), input, output);
    }


    @Test
    public void TicTacToe() {
        var res = toJasmin("TicTacToe");
        var input = "0 0\n"   //1
                + "1 1\n"   //2
                + "0 1\n"   //1
                + "0 2\n"   //2
                + "1 0\n"   //1
                + "2 0\n";  //2
        var winner = 2;
        var expected = "Congratulations, %d, you have won the game.".formatted(winner);
        var run = executeWith(res.jasmin(), input);
        var output = run.getFullOutput().trim();
        assertTrue("Game should finish with player 2 as a winner (Checking last phrase). ", output.endsWith(expected), expected, output);
    }

    @Test
    public void Turing() {
        var res = toJasmin("Turing");
        var expected = SpecsIo.read(RESOURCES_LOCATION + "/" + BASE_PATH + "/expected/Turing.log").trim();
        executeExpecting(res.jasmin(), expected);
    }


}
