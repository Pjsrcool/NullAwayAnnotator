/*
 * MIT License
 *
 * Copyright (c) 2020 Nima Karimipour
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.ucr.cs.css;

import edu.ucr.cs.css.out.MethodInfo;
import edu.ucr.cs.css.out.TrackerNode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serializer class where all generated files in Fix Serialization package is created through APIs
 * of this class.
 */
public class Serializer {
  /** Path to write field graph. */
  private final Path fieldGraphPath;
  /** Path to write method call graph. */
  private final Path callGraphPath;
  /** Path to write method info metadata. */
  private final Path methodInfoPath;

  public static final String FIELD_GRAPH_NAME = "field_graph.tsv";
  public static final String CALL_GRAPH_NAME = "call_graph.tsv";
  public static final String METHOD_INFO_NAME = "method_info.tsv";

  public Serializer(Config config) {
    Path outputDirectory = config.outputDirectory;
    this.fieldGraphPath = outputDirectory.resolve(FIELD_GRAPH_NAME);
    this.callGraphPath = outputDirectory.resolve(CALL_GRAPH_NAME);
    this.methodInfoPath = outputDirectory.resolve(METHOD_INFO_NAME);
    initializeOutputFiles(config);
  }

  /**
   * Appends the string representation of the {@link TrackerNode} corresponding to a call graph.
   *
   * @param callGraphNode TrackerNode instance.
   */
  public void serializeCallGraphNode(TrackerNode callGraphNode) {
    appendToFile(callGraphNode.toString(), this.callGraphPath);
  }

  /**
   * Appends the string representation of the {@link TrackerNode} corresponding to a field graph.
   *
   * @param fieldGraphNode TrackerNode instance.
   */
  public void serializeFieldGraphNode(TrackerNode fieldGraphNode) {
    appendToFile(fieldGraphNode.toString(), this.fieldGraphPath);
  }

  /**
   * Appends the string representation of the {@link MethodInfo} corresponding to a method.
   *
   * @param methodInfo MethodInfo instance.
   */
  public void serializeMethodInfo(MethodInfo methodInfo) {
    appendToFile(methodInfo.toString(), this.methodInfoPath);
  }

  /** Cleared the content of the file if exists and writes the header in the first line. */
  private void initializeFile(Path path, String header) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new RuntimeException("Could not clear file at: " + path, e);
    }
    try (OutputStream os = new FileOutputStream(path.toFile())) {
      header += "\n";
      os.write(header.getBytes(Charset.defaultCharset()), 0, header.length());
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException("Could not finish resetting File at Path: " + path, e);
    }
  }

  /** Initializes every file which will be re-generated in the new run of NullAway. */
  private void initializeOutputFiles(Config config) {
    try {
      Files.createDirectories(config.outputDirectory);
      if (config.callTrackerIsActive) {
        initializeFile(callGraphPath, TrackerNode.header());
      }
      if (config.fieldTrackerIsActive) {
        initializeFile(fieldGraphPath, TrackerNode.header());
      }
      if (config.methodTrackerIsActive) {
        initializeFile(methodInfoPath, MethodInfo.header());
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not finish resetting serializer", e);
    }
  }

  private void appendToFile(String row, Path path) {
    // Since there is no method available in API of either javac or errorprone to inform NullAway
    // that the analysis is finished, we cannot open a single stream and flush it within a finalize
    // method. Must open and close a new stream everytime we are appending a new line to a file.
    if (row == null || row.equals("")) {
      return;
    }
    row = row + "\n";
    try (OutputStream os = new FileOutputStream(path.toFile(), true)) {
      os.write(row.getBytes(Charset.defaultCharset()), 0, row.length());
      os.flush();
    } catch (IOException e) {
      throw new RuntimeException("Error happened for writing at file: " + path, e);
    }
  }
}
