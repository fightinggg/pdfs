package com.pdfs.normalfs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class File {
    public String name;
    public Boolean isDir;
    List<String> groups;


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
