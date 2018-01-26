package com.thefloow.gradle.gitversion
/*
 * This file is part of git-commit-id-plugin by Konrad 'ktoso' Malawski <konrad.malawski@java.pl>
 *
 * git-commit-id-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * git-commit-id-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with git-commit-id-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

import groovy.transform.Canonical;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevTag;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static java.time.ZoneOffset.*;

@Canonical
class DatedRevTag {

    AnyObjectId id
    String tagName
    LocalDateTime date

    DatedRevTag(RevTag tag) {
        this(tag.getId(), tag.getTagName(),
                (tag.getTaggerIdent() != null) ? LocalDateTime.ofInstant(tag.getTaggerIdent().getWhen().toInstant(), UTC) : LocalDateTime.now(UTC).minusYears(1900))
    }

    DatedRevTag(AnyObjectId id, String tagName) {
        this(id, tagName, LocalDateTime.now().minusYears(2000))
    }

    private DatedRevTag(AnyObjectId id, String tagName, LocalDateTime date) {
        this.id = id
        this.tagName = tagName
        this.date = date
    }
}
