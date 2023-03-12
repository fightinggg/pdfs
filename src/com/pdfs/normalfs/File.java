package com.pdfs.normalfs;

import java.util.Objects;

public class File {
    public String name;
    public Boolean isDir;

    public File(String name, Boolean isDir) {
        this.name = name;
        this.isDir = isDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(name, file.name) && Objects.equals(isDir, file.isDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isDir);
    }
}
