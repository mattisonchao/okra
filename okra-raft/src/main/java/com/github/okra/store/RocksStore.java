package com.github.okra.store;

import com.github.okra.model.LogEntry;
import com.github.okra.utils.HessianSerializeUtils;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksStore implements Store {

  private static final Logger logger = LoggerFactory.getLogger(RocksStore.class);
  private static final byte[] sizeSign = "SIZE".getBytes(StandardCharsets.UTF_8);
  private static final byte[] voteForSign = "VOTE_FOR".getBytes(StandardCharsets.UTF_8);
  private static final byte[] currentTerm = "CURRENT_TERM".getBytes(StandardCharsets.UTF_8);
  private final ReentrantLock lock = new ReentrantLock();
  private RocksDB kv;
  private RocksDB logs;

  {
    RocksDB.loadLibrary();
    Options options = new Options();
    options.setCreateIfMissing(true);

    File kvFile =
        new File(
            "/Users/mattison/Projects/openSource/mirror/temp/rocks-db" + UUID.randomUUID(),
            "mirror-kv");
    try {
      Files.createDirectories(kvFile.getParentFile().toPath());
      Files.createDirectories(kvFile.getAbsoluteFile().toPath());

      File logsFile =
          new File(
              "/Users/mattison/Projects/openSource/mirror/temp/rocks-db" + UUID.randomUUID(),
              "mirror-kv");
      Files.createDirectories(logsFile.getParentFile().toPath());
      Files.createDirectories(logsFile.getAbsoluteFile().toPath());
      kv = RocksDB.open(options, kvFile.getAbsolutePath());
      logs = RocksDB.open(options, logsFile.getAbsolutePath());
      logs.put("0".getBytes(StandardCharsets.UTF_8), HessianSerializeUtils.serialize(0));
      logger.info("RocksDB initialized and ready to use");
    } catch (IOException | RocksDBException e) {
      e.printStackTrace();
    }
  }

  private <T extends Serializable> T get(String key) {
    try {
      byte[] bytes = kv.get(key.getBytes(StandardCharsets.UTF_8));
      return HessianSerializeUtils.deserialize(bytes);
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return null;
  }

  private <T extends Serializable> void set(String key, T value) {
    byte[] serializedBytes = HessianSerializeUtils.serialize(value);
    try {
      kv.put(key.getBytes(StandardCharsets.UTF_8), serializedBytes);
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Integer getCurrentTerm() {
    try {
      byte[] bytes = kv.get(currentTerm);
      if (bytes == null) {
        return 0;
      } else {
        return Integer.valueOf(new String(bytes));
      }
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Integer setCurrentTerm(Integer term) {
    try {
      kv.put(currentTerm, term.toString().getBytes(StandardCharsets.UTF_8));
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Optional<InetSocketAddress> getVoteFor() {
    try {
      InetSocketAddress deserialize = HessianSerializeUtils.deserialize(kv.get(voteForSign));
      return Optional.ofNullable(deserialize);
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  @Override
  public void voteFor(InetSocketAddress candidate) {
    try {
      kv.put(voteForSign, HessianSerializeUtils.serialize(candidate));
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void clearVoteFor() {
    try {
      kv.put(voteForSign, "".getBytes(StandardCharsets.UTF_8));
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addLog(LogEntry logEntry) {
    try {
      lock.lock();
      Integer logSize = logSize();
      try {
        logs.put(
            logSize.toString().getBytes(StandardCharsets.UTF_8),
            HessianSerializeUtils.serialize(logEntry));
        logs.put(sizeSign, String.valueOf(logSize + 1).getBytes(StandardCharsets.UTF_8));
      } catch (RocksDBException e) {
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void addLogs(List<LogEntry> logEntries) {
    logEntries.forEach(this::addLog);
  }

  @Override
  public Optional<LogEntry> log(Integer index) {
    try {
      byte[] logEntryBytes = logs.get(index.toString().getBytes(StandardCharsets.UTF_8));
      if (logEntryBytes == null) {
        return Optional.empty();
      }
      LogEntry entry = HessianSerializeUtils.deserialize(logEntryBytes);
      return Optional.of(entry);
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  @Override
  public List<LogEntry> logs() {
    return subLog(0, logSize());
  }

  @Override
  public List<LogEntry> subLog(Integer startIndex, Integer endIndex) {
    if (logSize() == 0) {
      return Collections.emptyList();
    } else {
      List<LogEntry> entries = Lists.newArrayList();
      return IntStream.rangeClosed(startIndex, endIndex)
          .mapToObj(
              i -> {
                try {
                  byte[] bytes = logs.get(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
                  return (LogEntry) HessianSerializeUtils.deserialize(bytes);
                } catch (RocksDBException e) {
                  e.printStackTrace();
                }
                return null;
              })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
  }

  @Override
  public Integer logSize() {
    try {
      byte[] bytes = logs.get(sizeSign);
      if (bytes == null) {
        return 0;
      } else {
        return HessianSerializeUtils.deserialize(bytes);
      }
    } catch (RocksDBException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Integer lastLogIndex() {
    return logSize() - 1;
  }
}
