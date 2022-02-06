/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.pietelite.journey.common.search;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.event.FoundSolutionEvent;
import dev.pietelite.journey.common.search.event.SearchDispatcher;
import dev.pietelite.journey.common.search.event.SearchEvent;
import dev.pietelite.journey.common.search.event.StepSearchEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchTest {

  static boolean printResult = true;
  static boolean printSteps = false;

  static int boardSize = 12;
  static Point3D[][] board1 = new Point3D[boardSize][boardSize];
  static Point3D[][] board2 = new Point3D[boardSize][boardSize];
  Domain domain1 = new Domain("d1");
  Domain domain2 = new Domain("d2");

  @Test
  void findPath() {
    if (true) {
      return;
    }
    // Initialize domains to be complete.getY() free
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        board1[i][j] = new Point3D(i, j, domain1);
        board2[i][j] = new Point3D(i, j, domain2);
      }
    }

    // Add barriers
    board1[2][3] = null;
    board1[3][4] = null;
    board1[4][5] = null;
    board1[5][5] = null;
    board1[6][5] = null;
    board1[7][5] = null;
    board1[8][5] = null;
    board1[9][4] = null;
    board1[10][3] = null;
    board1[9][2] = null;
    board1[8][1] = null;
    board1[7][1] = null;
    board1[6][1] = null;
    board1[5][1] = null;
    board1[4][1] = null;
    board1[3][1] = null;
    board1[2][2] = null;

    board2[3][2] = null;
    board2[4][3] = null;
    board2[5][4] = null;
    board2[5][5] = null;
    board2[5][6] = null;
    board2[5][7] = null;
    board2[5][8] = null;
    board2[4][9] = null;
    board2[3][10] = null;

    // Set up JourneyCommon
    SearchDispatcher<Point3D, Domain, Runnable> dispatcher = new SearchDispatcher<>(Runnable::run);
    JourneyCommon.setSearchEventDispatcher(dispatcher);

    // Prepare variable to store if a solution has been found during the search
    AtomicBoolean solved = new AtomicBoolean(false);
    AtomicReference<Itinerary<Point3D, Domain>> solution = new AtomicReference<>();

    // Set up listeners for search event
    dispatcher.<FoundSolutionEvent<Point3D, Domain>>registerEvent(event -> () -> {
      solved.set(true);
      solution.set(event.getItinerary());
    }, SearchEvent.EventType.FOUND_SOLUTION);

    // Printers for our answers
    char[][] printer1 = new char[boardSize][boardSize];
    char[][] printer2 = new char[boardSize][boardSize];

    dispatcher.<StepSearchEvent<Point3D, Domain>>registerEvent(event -> () -> {
      if (!printSteps) {
        return;
      }
      char[][] printer;
      if (event.getStep().location().domain.equals(domain1)) {
        printer = printer1;
      } else {
        printer = printer2;
      }
      printer[event.getStep().location().getX()][event.getStep().location().getZ()] = '-';
    }, SearchEvent.EventType.STEP);

    Point3D origin = board1[4][4];
    Point3D destination = board1[4][8];

    // Set up parameters for search
    DestinationGoalSearchSession<Point3D, Domain> session = new TestSearchSession(UUID.randomUUID(),
        SearchSession.Caller.OTHER,
        origin,
        destination);
    List<Port<Point3D, Domain>> links = new LinkedList<>();
    links.add(new TestLink(board1[8][4], board2[3][6]));
    links.add(new TestLink(board2[7][7], board1[8][8]));
    links.forEach(session::registerPort);



    // Clear printer board
    clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize);

    session.registerMode(new StepMode(session));

    // Solve path
    Thread thread = new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      session.stop();
    });
    thread.start();
    session.search();

    Assertions.assertTrue(solved.get());

    if (printResult) {
      // Put in path
      solution.get().getSteps().forEach(step -> {
        char[][] printer;
        if (step.location().getDomain().equals(domain1)) {
          printer = printer1;
        } else {
          printer = printer2;
        }

        if (printer[step.location().getX()][step.location().getY()] == ' ') {
          printer[step.location().getX()][step.location().getY()] = '+';
        }
      });

      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    }

  }

  private void clearPrinters(Point3D[][] board1, Point3D[][] board2,
                             char[][] printer1, char[][] printer2,
                             Point3D origin, Point3D destination,
                             List<Port<Point3D, Domain>> ports,
                             int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board1[i][j] == null) {
          printer1[i][j] = '#';
        } else {
          printer1[i][j] = ' ';
        }
        if (board2[i][j] == null) {
          printer2[i][j] = '#';
        } else {
          printer2[i][j] = ' ';
        }
      }
    }

    // Put in origin and destination
    if (origin.getDomain().equals(domain1)) {
      printer1[origin.getX()][origin.getY()] = 'A';
    } else {
      printer2[origin.getX()][origin.getY()] = 'A';
    }
    if (origin.getDomain().equals(domain1)) {
      printer1[destination.getX()][destination.getY()] = 'B';
    } else {
      printer2[destination.getX()][destination.getY()] = 'B';
    }

    // Put in ports
    for (int i = 0; i < ports.size(); i++) {
      if (ports.get(i).getOrigin().getDomain().equals(domain1)) {
        printer1
            [ports.get(i).getOrigin().getX()]
            [ports.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (ports.get(i).getOrigin().getDomain().equals(domain2)) {
        printer2
            [ports.get(i).getOrigin().getX()]
            [ports.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (ports.get(i).getDestination().getDomain().equals(domain1)) {
        printer1
            [ports.get(i).getDestination().getX()]
            [ports.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
      if (ports.get(i).getDestination().getDomain().equals(domain2)) {
        printer2
            [ports.get(i).getDestination().getX()]
            [ports.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
    }
  }

  private void printPrinter(char[][] printer, int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer[j][i]);
      }
      System.out.print('\n');
    }
  }

  static class TestSearchSession extends DestinationGoalSearchSession<Point3D, Domain> {

    public TestSearchSession(UUID callerId, Caller callerType,
                             Point3D origin, Point3D destination) {
      super(callerId, callerType, origin, destination,
          (x, y, z, domain) -> new Point3D(x, y, new Domain(domain)));
    }

    @Override
    public long executionTime() {
      return 0;  // unimplemented
    }

  }

  public static class Point3D extends Cell<Point3D, Domain> {

    private final Domain domain;

    public Point3D(int x, int y, Domain domain) {
      super(x, y, 0, domain.name(), name -> domain);
      this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Point3D that = (Point3D) o;
      return this.coordinateX == that.coordinateX
          && this.coordinateY == that.coordinateY
          && this.coordinateZ == that.coordinateZ && this.getDomain().equals(that.getDomain());
    }

    @Override
    public int hashCode() {
      return Objects.hash(coordinateX, coordinateY, domain);
    }

    @Override
    public String toString() {
      return String.format("(%d, %d, %d, %s)", coordinateX, coordinateY, coordinateZ, domain.name());
    }

    @Override
    public double distanceToSquared(Point3D other) {
      return (getX() - other.getX()) * (getX() - other.getX())
          + (getY() - other.getY()) * (getY() - other.getY())
          + (getZ() - other.getZ()) * (getZ() - other.getZ());
    }
  }

  public static class TestLink extends Port<Point3D, Domain> {

    public TestLink(Point3D origin, Point3D destination) {
      super(origin, destination, ModeType.PORT, 1);
    }

    @Override
    public String toString() {
      return String.format("Port: {Origin: %s, Destination: %s}", getOrigin(), getDestination());
    }
  }

  @Value
  @Accessors(fluent = true)
  public static class Domain {
    @NonNull @Getter String name;
  }

  public class StepMode extends Mode<Point3D, Domain> {

    /**
     * General constructor.
     *
     * @param session the search session requesting information from this mode
     */
    public StepMode(SearchSession<Point3D, Domain> session) {
      super(session);
    }

    @Override
    public void collectDestinations(@NotNull Point3D origin, @NotNull List<Option> options) {
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i == 0 && j == 0) {
            continue;
          }
          if (origin.getX() + i < 0) {
            continue;
          }
          if (origin.getX() + i >= boardSize) {
            continue;
          }
          if (origin.getY() + j < 0) {
            continue;
          }
          if (origin.getY() + j >= boardSize) {
            continue;
          }
          Point3D adding = null;
          if (origin.getDomain().equals(domain1)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1
                && board1[origin.getX() + i][origin.getY()] == null
                && board1[origin.getX()][origin.getY() + j] == null) {
              continue;
            }
            adding = board1[origin.getX() + i][origin.getY() + j];
          }
          if (origin.getDomain().equals(domain2)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1
                && board2[origin.getX() + i][origin.getY()] == null
                && board2[origin.getX()][origin.getY() + j] == null) {
              continue;
            }
            adding = board2[origin.getX() + i][origin.getY() + j];
          }
          if (adding != null) {
            accept(adding, origin.distanceTo(adding), options);
          }
        }
      }
    }

    @Override
    public @NotNull ModeType getType() {
      return ModeType.WALK;
    }
  }

}