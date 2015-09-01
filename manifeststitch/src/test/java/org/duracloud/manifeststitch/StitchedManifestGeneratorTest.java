package org.duracloud.manifeststitch;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.client.ContentStore;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.domain.Content;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.impl.TsvManifestFormatter;
import org.duracloud.mill.db.model.ManifestItem;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(EasyMockRunner.class)
public class StitchedManifestGeneratorTest extends EasyMockSupport {
    private String spaceId = "space-id";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testGenerate() throws Exception {
        String sourceContentId = "content.dat" ;
        String sourceMd5 = "checksum";
        String chunkManifestContentId = sourceContentId + ChunksManifest.manifestSuffix;
        String chunkContentId = "content.dat" + ChunksManifest.chunkSuffix + "0000";
        String unchunkedContentId = "content1.dat";
        ContentStore store = createMock(ContentStore.class);
        Content content = createMock(Content.class);
        TsvManifestFormatter formatter = new TsvManifestFormatter();
        File unstitchedManifest = File.createTempFile("unstitched", "tsv");
        unstitchedManifest.deleteOnExit();
        
        BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(unstitchedManifest)));        
        writer.write(formatter.getHeader()+"\n");
        write(writer, formatter, chunkManifestContentId);
        write(writer, formatter, chunkContentId);
        write(writer, formatter, unchunkedContentId);
        writer.close();
        
        expect(store.getManifest(spaceId, ManifestFormat.TSV)).andReturn(new FileInputStream(unstitchedManifest));
        
        ChunksManifest manifest = new ChunksManifest(sourceContentId, "text/plain", 1000);
        manifest.setMD5OfSourceContent(sourceMd5);
        String xml = ManifestDocumentBinding.createDocumentFrom(manifest);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        expect(content.getStream()).andReturn(is);
        
        expect(store.getContent(spaceId, chunkManifestContentId)).andReturn(content);
        replayAll();
        StitchedManifestGenerator generator = new StitchedManifestGenerator(store);
        
        InputStream stitched = generator.generate(spaceId, ManifestFormat.TSV);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stitched));
        assertEquals(formatter.getHeader(), reader.readLine());
        assertTrue(reader.readLine().contains(sourceContentId));
        assertTrue(reader.readLine().contains(unchunkedContentId));
        reader.close();
    }

    private void write(BufferedWriter writer,
                       ManifestFormatter formatter,
                       String contentId) throws IOException {
        ManifestItem item = new ManifestItem();
        item.setContentChecksum("checksum-md5");
        item.setContentId(contentId);
        item.setSpaceId(spaceId);
        writer.write(formatter.formatLine(item) +"\n");

    }

}
