Prefix(owl:=<http://www.w3.org/2002/07/owl#>)
Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)
Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)
Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)
Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)
Prefix(:=<http://xowl.org/platform/schemas/kernel#>)

Ontology(<http://xowl.org/platform/schemas/kernel>
    DataPropertyAssertion(rdfs:label <http://xowl.org/platform/schemas/kernel> "Kernel Schema")


    Declaration(Class(:Resource))
    DataPropertyAssertion(rdfs:label :Resource "Resource")
    DataPropertyAssertion(rdfs:comment :Resource "Represents a resource on a xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Resource <http://xowl.org/platform/schemas/kernel>)

    Declaration(Class(:Artifact))
    DataPropertyAssertion(rdfs:label :Artifact "Artifact")
    DataPropertyAssertion(rdfs:comment :Artifact "Represents an artifact, i.e. a self-contained package of data.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Artifact <http://xowl.org/platform/schemas/kernel>)
    SubClassOf(:Artifact :Resource)

    Declaration(Class(:User))
    DataPropertyAssertion(rdfs:label :User "User")
    DataPropertyAssertion(rdfs:comment :User "Represents a user of the xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :User <http://xowl.org/platform/schemas/kernel>)
    SubClassOf(:User :Resource)

    Declaration(Class(:Rule))
    DataPropertyAssertion(rdfs:label :Rule "Consistency Rule")
    DataPropertyAssertion(rdfs:comment :Rule "Represents a consistency rule on the xOWL federation platform.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Rule <http://xowl.org/platform/schemas/kernel>)
    SubClassOf(:Rule :Resource)

    Declaration(Class(:Inconsistency))
    DataPropertyAssertion(rdfs:label :Inconsistency "Inconsistency")
    DataPropertyAssertion(rdfs:comment :Inconsistency "Represents an inconsistency produced by a consistency rule.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :Inconsistency <http://xowl.org/platform/schemas/kernel>)
    SubClassOf(:Inconsistency :Resource)



    Declaration(DataProperty(:name))
    FunctionalDataProperty(:name)
    DataPropertyDomain(:name :Resource)
    DataPropertyRange(:name xsd:string)
    DataPropertyAssertion(rdfs:label :name "name")
    DataPropertyAssertion(rdfs:comment :name "The human readable name of a resource.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :name <http://xowl.org/platform/schemas/kernel>)

    Declaration(ObjectProperty(:creator))
    FunctionalObjectProperty(:creator)
    ObjectPropertyDomain(:creator :Resource)
    ObjectPropertyRange(:creator :User)
    DataPropertyAssertion(rdfs:label :creator "creator")
    DataPropertyAssertion(rdfs:comment :creator "The user that created the resource.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :creator <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:created))
    FunctionalDataProperty(:created)
    DataPropertyDomain(:created :Resource)
    DataPropertyRange(:created xsd:date)
    DataPropertyAssertion(rdfs:label :created "created")
    DataPropertyAssertion(rdfs:comment :created "The date at which the resource has been initially created.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :created <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:modified))
    FunctionalDataProperty(:modified)
    DataPropertyDomain(:modified :Resource)
    DataPropertyRange(:modified xsd:date)
    DataPropertyAssertion(rdfs:label :modified "modified")
    DataPropertyAssertion(rdfs:comment :modified "The date at which the resource has been modified for the last time.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :modified <http://xowl.org/platform/schemas/kernel>)


    Declaration(ObjectProperty(:base))
    FunctionalObjectProperty(:base)
    ObjectPropertyDomain(:base :Artifact)
    DataPropertyAssertion(rdfs:label :base "base")
    DataPropertyAssertion(rdfs:comment :base "The base element of which the artifact is a version of.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :base <http://xowl.org/platform/schemas/kernel>)

    Declaration(ObjectProperty(:archetype))
    FunctionalObjectProperty(:archetype)
    ObjectPropertyDomain(:archetype :Artifact)
    DataPropertyAssertion(rdfs:label :archetype "archetype")
    DataPropertyAssertion(rdfs:comment :archetype "The base archetype of the artifact.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :archetype <http://xowl.org/platform/schemas/kernel>)

    Declaration(ObjectProperty(:supersedes))
    ObjectPropertyDomain(:supersedes :Artifact)
    ObjectPropertyRange(:supersedes :Artifact)
    DataPropertyAssertion(rdfs:label :supersedes "supersedes")
    DataPropertyAssertion(rdfs:comment :supersedes "The artifacts that are superseded by this one.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :supersedes <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:version))
    FunctionalDataProperty(:version)
    DataPropertyDomain(:version :Artifact)
    DataPropertyRange(:version xsd:string)
    DataPropertyAssertion(rdfs:label :version "version")
    DataPropertyAssertion(rdfs:comment :version "The version tag of an artifact.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :version <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:from))
    FunctionalDataProperty(:from)
    DataPropertyDomain(:from :Artifact)
    DataPropertyRange(:from xsd:string)
    DataPropertyAssertion(rdfs:label :from "from")
    DataPropertyAssertion(rdfs:comment :from "The identifier of the connector from which the artifact has been imported.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :from <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:definition))
    FunctionalDataProperty(:definition)
    DataPropertyDomain(:definition :Rule)
    DataPropertyRange(:definition xsd:string)
    DataPropertyAssertion(rdfs:label :definition "definition")
    DataPropertyAssertion(rdfs:comment :definition "The definition of a consistency rule.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :definition <http://xowl.org/platform/schemas/kernel>)

    Declaration(DataProperty(:message))
    FunctionalDataProperty(:message)
    DataPropertyDomain(:message :Inconsistency)
    DataPropertyRange(:message xsd:string)
    DataPropertyAssertion(rdfs:label :message "message")
    DataPropertyAssertion(rdfs:comment :message "The warning message for an inconsistency.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :message <http://xowl.org/platform/schemas/kernel>)

    Declaration(ObjectProperty(:producedBy))
    FunctionalObjectProperty(:producedBy)
    ObjectPropertyDomain(:producedBy :Inconsistency)
    ObjectPropertyRange(:producedBy :Rule)
    DataPropertyAssertion(rdfs:label :producedBy "producedBy")
    DataPropertyAssertion(rdfs:comment :producedBy "The rule that produced the inconsistency.")
    ObjectPropertyAssertion(rdfs:isDefinedBy :producedBy <http://xowl.org/platform/schemas/kernel>)
)