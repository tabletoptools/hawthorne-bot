package io.tabletoptools.hawthorne;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void testConfigurationPresent() {
        Config config = Config.instance();
        String prefix = config.getString("prefix");
        Assert.assertEquals("testprefix", prefix);
    }

}
