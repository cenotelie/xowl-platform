Prefix(owl:=<http://www.w3.org/2002/07/owl#>)
Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)
Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)
Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)
Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)
Prefix(:=<http://xowl.org/platform/kernel#>)

Ontology(<http://xowl.org/platform/kernel>
    DataPropertyAssertion(rdfs:label <http://xowl.org/platform/kernel> "Kernel Schema")


    Declaration(Class(:Resource))
    DataPropertyAssertion(rdfs:label :Resource "Resource")
    DataPropertyAssertion(rdfs:comment :Resource "Represents a resource on a xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Resource <http://xowl.org/platform/kernel>)

    Declaration(Class(:Artifact))
    DataPropertyAssertion(rdfs:label :Artifact "Artifact")
    DataPropertyAssertion(rdfs:comment :Artifact "Represents an artifact, i.e. a self-contained package of data.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Artifact <http://xowl.org/platform/kernel>)
    SubClassOf(:Artifact :Resource)

    Declaration(Class(:PlatformUser))
    DataPropertyAssertion(rdfs:label :PlatformUser "PlatformUser")
    DataPropertyAssertion(rdfs:comment :PlatformUser "Represents a user of the xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :PlatformUser <http://xowl.org/platform/kernel>)
    SubClassOf(:PlatformUser :Resource)

    Declaration(Class(:PlatformGroup))
    DataPropertyAssertion(rdfs:label :PlatformGroup "PlatformGroup")
    DataPropertyAssertion(rdfs:comment :PlatformGroup "Represents a group of users on the xOWL federation platform")
    ObjectPropertyAssertion(rdfs:isDefinedBy :PlatformGroup <http://xowl.org/platform/kernel>)
    SubClassOf(:PlatformGroup :Resource)

    Declaration(Class(:PlatformRole))
    DataPropertyAssertion(rdfs:label :PlatformRole "PlatformRole")
    DataPropertyAssertion(rdfs:comment :PlatformRole "Represents a role for a users or groups on the xOWL federation platform")
    ObjectPropertyAssertion(rdfs:isDefinedBy :PlatformRole <http://xowl.org/platform/kernel>)
    SubClassOf(:PlatformRole :Resource)

    Declaration(Class(:Rule))
    DataPropertyAssertion(rdfs:label :Rule "Consistency Rule")
    DataPropertyAssertion(rdfs:comment :Rule "Represents a consistency rule on the xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Rule <http://xowl.org/platform/kernel>)
    SubClassOf(:Rule :Resource)

    Declaration(Class(:Inconsistency))
    DataPropertyAssertion(rdfs:label :Inconsistency "Inconsistency")
    DataPropertyAssertion(rdfs:comment :Inconsistency "Represents an inconsistency produced by a consistency rule.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Inconsistency <http://xowl.org/platform/kernel>)
    SubClassOf(:Inconsistency :Resource)



    Declaration(DataProperty(:name))
    FunctionalDataProperty(:name)
    DataPropertyDomain(:name :Resource)
    DataPropertyRange(:name xsd:string)
    DataPropertyAssertion(rdfs:label :name "name")
    DataPropertyAssertion(rdfs:comment :name "The human readable name of a resource.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :name <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:creator))
    FunctionalObjectProperty(:creator)
    ObjectPropertyDomain(:creator :Resource)
    ObjectPropertyRange(:creator :User)
    DataPropertyAssertion(rdfs:label :creator "creator")
    DataPropertyAssertion(rdfs:comment :creator "The user that created the resource.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :creator <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:created))
    FunctionalDataProperty(:created)
    DataPropertyDomain(:created :Resource)
    DataPropertyRange(:created xsd:date)
    DataPropertyAssertion(rdfs:label :created "created")
    DataPropertyAssertion(rdfs:comment :created "The date at which the resource has been initially created.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :created <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:modified))
    FunctionalDataProperty(:modified)
    DataPropertyDomain(:modified :Resource)
    DataPropertyRange(:modified xsd:date)
    DataPropertyAssertion(rdfs:label :modified "modified")
    DataPropertyAssertion(rdfs:comment :modified "The date at which the resource has been modified for the last time.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :modified <http://xowl.org/platform/kernel>)


    Declaration(ObjectProperty(:base))
    FunctionalObjectProperty(:base)
    ObjectPropertyDomain(:base :Artifact)
    DataPropertyAssertion(rdfs:label :base "base")
    DataPropertyAssertion(rdfs:comment :base "The base element of which the artifact is a version of.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :base <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:archetype))
    FunctionalObjectProperty(:archetype)
    ObjectPropertyDomain(:archetype :Artifact)
    DataPropertyAssertion(rdfs:label :archetype "archetype")
    DataPropertyAssertion(rdfs:comment :archetype "The base archetype of the artifact.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :archetype <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:supersedes))
    ObjectPropertyDomain(:supersedes :Artifact)
    ObjectPropertyRange(:supersedes :Artifact)
    DataPropertyAssertion(rdfs:label :supersedes "supersedes")
    DataPropertyAssertion(rdfs:comment :supersedes "The artifacts that are superseded by this one.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :supersedes <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:version))
    FunctionalDataProperty(:version)
    DataPropertyDomain(:version :Artifact)
    DataPropertyRange(:version xsd:string)
    DataPropertyAssertion(rdfs:label :version "version")
    DataPropertyAssertion(rdfs:comment :version "The version tag of an artifact.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :version <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:from))
    FunctionalDataProperty(:from)
    DataPropertyDomain(:from :Artifact)
    DataPropertyRange(:from xsd:string)
    DataPropertyAssertion(rdfs:label :from "from")
    DataPropertyAssertion(rdfs:comment :from "The identifier of the connector from which the artifact has been imported.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :from <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:definition))
    FunctionalDataProperty(:definition)
    DataPropertyDomain(:definition :Rule)
    DataPropertyRange(:definition xsd:string)
    DataPropertyAssertion(rdfs:label :definition "definition")
    DataPropertyAssertion(rdfs:comment :definition "The definition of a consistency rule.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :definition <http://xowl.org/platform/kernel>)

    Declaration(DataProperty(:message))
    FunctionalDataProperty(:message)
    DataPropertyDomain(:message :Inconsistency)
    DataPropertyRange(:message xsd:string)
    DataPropertyAssertion(rdfs:label :message "message")
    DataPropertyAssertion(rdfs:comment :message "The warning message for an inconsistency.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :message <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:producedBy))
    FunctionalObjectProperty(:producedBy)
    ObjectPropertyDomain(:producedBy :Inconsistency)
    ObjectPropertyRange(:producedBy :Rule)
    DataPropertyAssertion(rdfs:label :producedBy "producedBy")
    DataPropertyAssertion(rdfs:comment :producedBy "The rule that produced the inconsistency.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :producedBy <http://xowl.org/platform/kernel>)


    Declaration(ObjectProperty(:hasMember))
    ObjectPropertyDomain(:hasMember :PlatformGroup)
    ObjectPropertyRange(:hasMember :PlatformUser)
    DataPropertyAssertion(rdfs:label :hasMember "hasMember")
    DataPropertyAssertion(rdfs:comment :hasMember "The members of the group.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :hasMember <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:hasAdmin))
    ObjectPropertyDomain(:hasAdmin :PlatformGroup)
    ObjectPropertyRange(:hasAdmin :PlatformUser)
    DataPropertyAssertion(rdfs:label :hasAdmin "hasAdmin")
    DataPropertyAssertion(rdfs:comment :hasAdmin "The administrators of the group.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :hasAdmin <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:hasRole))
    ObjectPropertyDomain(:hasRole ObjectUnionOf(:PlatformUser :PlatformGroup))
    ObjectPropertyRange(:hasRole :PlatformRole)
    DataPropertyAssertion(rdfs:label :hasRole "hasRole")
    DataPropertyAssertion(rdfs:comment :hasRole "The roles of the user or group")
    ObjectPropertyAssertion(rdfs:isDefinedBy :hasRole <http://xowl.org/platform/kernel>)

    Declaration(ObjectProperty(:impliesRole))
    ObjectPropertyDomain(:impliesRole :PlatformRole)
    ObjectPropertyRange(:impliesRole :PlatformRole)
    DataPropertyAssertion(rdfs:label :impliesRole "impliesRole")
    DataPropertyAssertion(rdfs:comment :impliesRole "The roles implied by this role")
    ObjectPropertyAssertion(rdfs:isDefinedBy :impliesRole <http://xowl.org/platform/kernel>)
)