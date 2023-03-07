package com.petflix.films

import com.google.common.io.Files
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import org.slf4j.LoggerFactory

class ProtoLoader {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    val protoSpec = ProtoLoader::class.java.classLoader.getResource("proto/new-releases.proto")
        .readText()

    val protobufSchema = loadProtoSchema()

    private fun loadProtoSchema(): Schema {
        val temp = Files.createTempDir()
        val protoDir = temp.resolve("proto/")
        protoDir.mkdirs()
        val protoFile = protoDir.resolve("new-releases.proto")
        protoFile
            .writeText(protoSpec)

        logger.info("Wrote protobuf spec to temp file: ${protoFile.canonicalPath}")

        return SchemaLoader(FileSystem.SYSTEM).apply {
            this.initRoots(listOf(Location.get(protoDir.absolutePath)))
        }
            .loadSchema()

    }
}
