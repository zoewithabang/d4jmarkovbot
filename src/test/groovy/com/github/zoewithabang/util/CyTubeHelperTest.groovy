package com.github.zoewithabang.util

import com.github.zoewithabang.model.CyTubeMedia
import spock.lang.Specification

class CyTubeHelperTest extends Specification
{
    def "now playing youtube"()
    {
        when:
        def log = new File(getClass().getResource("/cytubechannel_yt.log").toURI())
        def media = CyTubeHelper.getLatestNowPlaying(log)
        def expected = new CyTubeMedia("Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended", "yt", "h-rj8HVW3PQ")

        then:
        media == expected
    }

    def "now playing soundcloud"()
    {
        when:
        def log = new File(getClass().getResource("/cytubechannel_sc.log").toURI())
        def media = CyTubeHelper.getLatestNowPlaying(log)
        def expected = new CyTubeMedia("影踏み [\"Simultaneity\" OUTTRACK]", "sc", "https://soundcloud.com/charlot-1/kagefumi")

        then:
        media == expected
    }

    def "no now playing found returning empty media object"()
    {
        when:
        def log = new File(getClass().getResource("/cytubechannel_none.log").toURI())
        def media = CyTubeHelper.getLatestNowPlaying(log)
        def expected = new CyTubeMedia()

        then:
        media == expected
    }
}