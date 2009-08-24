package org.apache.vysper.xmpp.modules.extension.xep0045_muc.handler;

import java.util.List;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Affiliation;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Role;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Room;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.protocol.ProtocolException;
import org.apache.vysper.xmpp.protocol.ResponseStanzaContainer;
import org.apache.vysper.xmpp.protocol.StanzaHandler;
import org.apache.vysper.xmpp.stanza.PresenceStanza;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.apache.vysper.xmpp.xmlfragment.XMLElement;
import org.apache.vysper.xmpp.xmlfragment.XMLSemanticError;

/**
 */
public class MUCPresenceHandlerChangeStatusTestCase extends AbstractMUCHandlerTestCase {

    private Stanza changeStatus(Entity occupantJid, Entity roomWithNickJid, String show, String status) throws ProtocolException {
        StanzaBuilder stanzaBuilder = StanzaBuilder.createPresenceStanza(occupantJid, roomWithNickJid, null, null, show, status);
        
        Stanza presenceStanza = stanzaBuilder.getFinalStanza();
        ResponseStanzaContainer container = handler.execute(presenceStanza, sessionContext.getServerRuntimeContext(), true, sessionContext, null);
        if(container != null) {
            return container.getResponseStanza();
        } else {
            return null;
        }
    }
    

    @Override
    protected StanzaHandler createHandler() {
        return new MUCPresenceHandler(conference);
    }
    
    public void testChangeShowStatus() throws Exception {
        Room room = conference.findRoom(ROOM1_JID);
        room.addOccupant(OCCUPANT1_JID, "nick");
        room.addOccupant(OCCUPANT2_JID, "nick 2");

        assertNull(changeStatus(OCCUPANT1_JID, ROOM1_JID_WITH_NICK, "xa", "Gone"));
        
        MUCUserItem item = new MUCUserItem(OCCUPANT1_JID, "nick", Affiliation.None, Role.Participant);
        assertPresenceStanza(occupant1Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT1_JID, "xa", "Gone",
                item);
        assertPresenceStanza(occupant2Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT2_JID, "xa", "Gone", 
                item);
    }
    
    
    public void testChangeShow() throws Exception {
        Room room = conference.findRoom(ROOM1_JID);
        room.addOccupant(OCCUPANT1_JID, "nick");
        room.addOccupant(OCCUPANT2_JID, "nick 2");

        assertNull(changeStatus(OCCUPANT1_JID, ROOM1_JID_WITH_NICK, "xa", null));
        
        MUCUserItem item = new MUCUserItem(OCCUPANT1_JID, "nick", Affiliation.None, Role.Participant);
        assertPresenceStanza(occupant1Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT1_JID, "xa", null,
                item);
        assertPresenceStanza(occupant2Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT2_JID, "xa", null, 
                item);
    }
    
    
    public void testChangeStatus() throws Exception {
        Room room = conference.findRoom(ROOM1_JID);
        room.addOccupant(OCCUPANT1_JID, "nick");
        room.addOccupant(OCCUPANT2_JID, "nick 2");

        assertNull(changeStatus(OCCUPANT1_JID, ROOM1_JID_WITH_NICK, null, "Gone"));
        
        MUCUserItem item = new MUCUserItem(OCCUPANT1_JID, "nick", Affiliation.None, Role.Participant);
        assertPresenceStanza(occupant1Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT1_JID, null, "Gone",
                item);
        assertPresenceStanza(occupant2Queue.getNext(), ROOM1_JID_WITH_NICK, OCCUPANT2_JID, null, "Gone", 
                item);
    }
    
    private void assertPresenceStanza(Stanza stanza, Entity expectedFrom, Entity expectedTo, String expectedShow,
            String expectedStatus,
            MUCUserItem expectedItem) throws XMLSemanticError {

        PresenceStanza presenceStanza = (PresenceStanza) PresenceStanza.getWrapper(stanza);
        assertNotNull(stanza);
        assertEquals(expectedFrom, stanza.getFrom());
        assertEquals(expectedTo, stanza.getTo());
        assertEquals(expectedShow, presenceStanza.getShow());
        assertEquals(expectedStatus, presenceStanza.getStatus(null));
        
        XMLElement xElm = stanza.getSingleInnerElementsNamed("x");
        assertEquals(NamespaceURIs.XEP0045_MUC_USER, xElm.getNamespaceURI());
        
        List<XMLElement> innerElements = xElm.getInnerElements();
            
        assertEquals(1, innerElements.size());
        XMLElement itemElm = innerElements.get(0);
        assertEquals("item", itemElm.getName());
        assertEquals(expectedItem.getJid().getFullQualifiedName(), itemElm.getAttributeValue("jid"));
        assertEquals(expectedItem.getNick(), itemElm.getAttributeValue("nick"));
        assertEquals(expectedItem.getAffiliation().toString(), itemElm.getAttributeValue("affiliation"));
        assertEquals(expectedItem.getRole().toString(), itemElm.getAttributeValue("role"));
        
    }
    
}
