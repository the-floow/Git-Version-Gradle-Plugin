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

class GitCommitPropertyConstant {
    // these properties will be exposed to maven
    static final String BRANCH = "branch";
    static final String DIRTY = "dirty";
    // only one of the following two will be exposed, depending on the commitIdGenerationMode
    static final String COMMIT_ID_FLAT = "commit.id";
    static final String COMMIT_ID_ABBREV = "commit.id.abbrev";
    static final String COMMIT_DESCRIBE = "commit.id.describe";
    static final String COMMIT_SHORT_DESCRIBE = "commit.id.describe-short";
    static final String BUILD_AUTHOR_NAME = "build.user.name";
    static final String BUILD_AUTHOR_EMAIL = "build.user.email";
    static final String BUILD_TIME = "build.time";
    static final String BUILD_VERSION = "build.version";
    static final String BUILD_HOST = "build.host";
    static final String COMMIT_AUTHOR_NAME = "commit.user.name";
    static final String COMMIT_AUTHOR_EMAIL = "commit.user.email";
    static final String COMMIT_MESSAGE_FULL = "commit.message.full";
    static final String COMMIT_MESSAGE_SHORT = "commit.message.short";
    static final String COMMIT_TIME = "commit.time";
    static final String REMOTE_ORIGIN_URL = "remote.origin.url";
    static final String TAGS = "tags";
    static final String CLOSEST_TAG_NAME = "closest.tag.name";
    static final String CLOSEST_TAG_COMMIT_COUNT = "closest.tag.commit.count";

}
