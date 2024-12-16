import logging
import threading
import time
import urllib.parse
import urllib.request
import uuid
import os

from locust import FastHttpUser, between, events, task
from locust.runners import MasterRunner

HEALTH_ENDPOINT = "/health"
AUTH_ENDPOINT = "/v1/auth/token"
ORDERS_ENDPOINT = "/v1/etor/orders"
RESULTS_ENDPOINT = "/v1/etor/results"
METADATA_ENDPOINT = "/v1/etor/metadata"
CONSOLIDATED_ENDPOINT = "/v1/etor/metadata/summary"

order_request_body = None
result_request_body = None
auth_request_body = None


class SampleUser(FastHttpUser):
    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks.
    # Tasks with the same number get called approx. the same number of times.

    token_refresh_interval = 280
    access_token = None
    wait_time = between(1, 5)

    def on_start(self):
        self.authenticate()

        self.submission_id = None
        self.placer_order_id = None
        self.message_api_called = False
        self.sender = "flexion.simulated-hospital"

        # Start the token refreshing thread
        threading.Thread(
            target=self.authenticate_periodically, args=(), daemon=True
        ).start()

    def authenticate_periodically(self):
        while True:
            time.sleep(self.token_refresh_interval)  # Refresh token every few seconds
            self.authenticate()

    def authenticate(self):
        logging.debug("Authenticating...")
        response = self.client.post(AUTH_ENDPOINT, data=auth_request_body)
        if response.status_code == 200:
            data = response.json()
            self.access_token = data["access_token"]
        else:
            logging.error(f"Authentication failed: {response.error}")

    @task(1)
    def get_health(self):
        self.client.get(HEALTH_ENDPOINT)

    def post_message_request(self, endpoint, message):
        self.submission_id = str(uuid.uuid4())
        poi = self.placer_order_id or str(uuid.uuid4())
        self.placer_order_id = None if self.placer_order_id else poi
        response = self.client.post(
            endpoint,
            headers={
                "Authorization": self.access_token,
                "RecordId": self.submission_id,
            },
            data=message.replace("{{placer_order_id}}", poi),
        )
        if response.status_code == 200:
            self.message_api_called = True

    @task(5)
    def post_v1_etor_orders(self):
        self.post_message_request(ORDERS_ENDPOINT, order_request_body)

    @task(5)
    def post_v1_etor_results(self):
        self.post_message_request(RESULTS_ENDPOINT, result_request_body)

    @task(1)
    def get_v1_etor_metadata(self):
        if self.message_api_called:
            self.client.get(
                f"{METADATA_ENDPOINT}/{self.submission_id}",
                headers={"Authorization": self.access_token},
                name=f"{METADATA_ENDPOINT}/{{id}}",
            )

    @task(1)
    def get_v1_metadata_consolidated(self):
        if self.message_api_called:
            self.client.get(
                f"{CONSOLIDATED_ENDPOINT}/{self.sender}",
                headers={"Authorization": self.access_token},
            )


@events.test_start.add_listener
def test_start(environment):
    global auth_request_body
    global order_request_body
    global result_request_body

    if isinstance(environment.runner, MasterRunner):
        # in a distributed run, the master does not typically need any test data
        return

    auth_request_body = get_auth_request_body()
    order_request_body = get_order_fhir_message()
    result_request_body = get_result_fhir_message()


@events.quitting.add_listener
def assert_stats(environment):
    if environment.stats.total.fail_ratio > 0.01:
        logging.error("Test failed due to failure ratio > 1%")
        environment.process_exit_code = 1
    elif environment.stats.total.get_response_time_percentile(0.95) > 1000:
        logging.error("Test failed due to 95th percentile response time > 1000 ms")
        environment.process_exit_code = 1
    else:
        logging.info("Test passed!")


def get_auth_request_body():
    # set up the sample request body for the auth endpoint
    # using a valid test token found in the mock_credentials directory

    jwt = "trusted-intermediary-valid-token.jwt"

    auth_token = os.getenv(jwt.replace('.', '-'))

    if auth_token is None:
        with open(f"mock_credentials/{jwt}") as f:
            auth_token = f.read()

    params = urllib.parse.urlencode(
        {"scope": "trusted-intermediary", "client_assertion": auth_token.strip()}
    )

    return params.encode("utf-8")


def get_order_fhir_message():
    # read the sample request body for the orders endpoint
    return """{
    "resourceType": "Bundle",
    "id": "1713991685806650392.f865cc8e-d438-4d5f-9147-05930f25a997",
    "meta": {
        "lastUpdated": "2024-04-24T20:48:05.813+00:00"
    },
    "identifier": {
        "system": "https://reportstream.cdc.gov/prime-router",
        "value": "31808297"
    },
    "type": "message",
    "timestamp": "2023-05-06T10:29:16.000+00:00",
    "entry": [
        {
            "fullUrl": "MessageHeader/87c2d0db-6f31-3666-b9e2-7152e039c11f",
            "resource": {
                "resourceType": "MessageHeader",
                "id": "87c2d0db-6f31-3666-b9e2-7152e039c11f",
                "meta": {
                    "tag": [
                        {
                            "system": "http://terminology.hl7.org/CodeSystem/v2-0103",
                            "code": "P"
                        }
                    ]
                },
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/msh-message-header",
                        "extension": [
                            {
                                "url": "MSH.7",
                                "valueString": "20230506052916-0500"
                            },
                            {
                                "url": "MSH.15",
                                "valueString": "AL"
                            },
                            {
                                "url": "MSH.16",
                                "valueString": "AL"
                            },
                            {
                                "url": "MSH.21",
                                "valueIdentifier": {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                            "extension": [
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                                                    "valueString": "2.16.840.1.113883.9.82"
                                                },
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                                                    "valueCode": "ISO"
                                                }
                                            ]
                                        }
                                    ],
                                    "value": "LAB_PRU_COMPONENT"
                                }
                            },
                            {
                                "url": "MSH.21",
                                "valueIdentifier": {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                            "extension": [
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                                                    "valueString": "2.16.840.1.113883.9.22"
                                                },
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                                                    "valueCode": "ISO"
                                                }
                                            ]
                                        }
                                    ],
                                    "value": "LAB_TO_COMPONENT"
                                }
                            }
                        ]
                    }
                ],
                "eventCoding": {
                    "system": "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "code": "O01",
                    "display": "ORM^O01^ORM_O01"
                },
                "destination": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                                "valueString": "natus.health.state.mn.us"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                                "valueString": "DNS"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "MSH.5"
                            }
                        ],
                        "name": "NATUS",
                        "receiver": {
                            "reference": "Organization/1713991685926824266.a1dc31ff-2719-470f-9a9d-2bfd3d6353e3"
                        }
                    }
                ],
                "sender": {
                    "reference": "Organization/1713991685881552998.10ccacc7-1879-4a11-b1cc-c1f87830d494"
                },
                "source": {
                    "extension": [
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                            "valueString": "Epic"
                        },
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                            "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                        },
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                            "valueString": "ISO"
                        },
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                            "valueString": "MSH.3"
                        }
                    ],
                    "endpoint": "urn:oid:1.2.840.114350.1.13.145.2.7.2.695071"
                }
            }
        },
        {
            "fullUrl": "Organization/1713991685881552998.10ccacc7-1879-4a11-b1cc-c1f87830d494",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991685881552998.10ccacc7-1879-4a11-b1cc-c1f87830d494",
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.1"
                            }
                        ],
                        "value": "Centracare"
                    },
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.2,HD.3"
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0301",
                                    "code": "DNS"
                                }
                            ]
                        },
                        "value": "centracare.com"
                    }
                ]
            }
        },
        {
            "fullUrl": "Organization/1713991685926824266.a1dc31ff-2719-470f-9a9d-2bfd3d6353e3",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991685926824266.a1dc31ff-2719-470f-9a9d-2bfd3d6353e3",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        "valueString": "MSH.6"
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.1"
                            }
                        ],
                        "value": "MN Public Health Lab"
                    },
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.2,HD.3"
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0301",
                                    "code": "ISO"
                                }
                            ]
                        },
                        "system": "urn:ietf:rfc:3986",
                        "value": "2.16.840.1.114222.4.1.10080"
                    }
                ]
            }
        },
        {
            "fullUrl": "Patient/1713991686420540992.4160b099-3871-449c-9e90-dadeb21100e7",
            "resource": {
                "resourceType": "Patient",
                "id": "1713991686420540992.4160b099-3871-449c-9e90-dadeb21100e7",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/pid-patient",
                        "extension": [
                            {
                                "url": "PID.8"
                            },
                            {
                                "url": "PID.24",
                                "valueString": "N"
                            },
                            {
                                "url": "PID.30",
                                "valueString": "N"
                            }
                        ]
                    },
                    {
                        "url": "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName",
                        "valueHumanName": {
                            "extension": [
                                {
                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name",
                                    "extension": [
                                        {
                                            "url": "XPN.2",
                                            "valueString": "SADIE"
                                        },
                                        {
                                            "url": "XPN.3",
                                            "valueString": "S"
                                        }
                                    ]
                                }
                            ],
                            "family": "SMITH",
                            "given": [
                                "SADIE",
                                "S"
                            ]
                        }
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cx-identifier",
                                "extension": [
                                    {
                                        "url": "CX.5",
                                        "valueString": "MR"
                                    }
                                ]
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "PID.3"
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "code": "MR"
                                }
                            ]
                        },
                        "value": "11102779",
                        "assigner": {
                            "reference": "Organization/1713991686376793169.d63d39d1-bd81-4180-a007-1a963547ff8d"
                        }
                    }
                ],
                "name": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name",
                                "extension": [
                                    {
                                        "url": "XPN.2",
                                        "valueString": "BB SARAH"
                                    },
                                    {
                                        "url": "XPN.7",
                                        "valueString": "L"
                                    }
                                ]
                            }
                        ],
                        "use": "official",
                        "text": "TestValue",
                        "family": "SMITH",
                        "given": [
                            "BB SARAH"
                        ]
                    }
                ],
                "telecom": [
                    {
                        "extension": [
                            {
                                "url": "http://hl7.org/fhir/StructureDefinition/contactpoint-area",
                                "valueString": "763"
                            },
                            {
                                "url": "http://hl7.org/fhir/StructureDefinition/contactpoint-local",
                                "valueString": "5555555"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xtn-contact-point",
                                "extension": [
                                    {
                                        "url": "XTN.3",
                                        "valueString": "PH"
                                    },
                                    {
                                        "url": "XTN.7",
                                        "valueString": "5555555"
                                    },
                                    {
                                        "url": "XTN.9",
                                        "valueString": "(763)555-5555"
                                    }
                                ]
                            }
                        ],
                        "system": "phone",
                        "use": "home"
                    }
                ],
                "gender": "male",
                "birthDate": "2023-05-04",
                "_birthDate": {
                    "extension": [
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
                            "valueString": "20230504131023-0500"
                        },
                        {
                            "url": "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
                            "valueDateTime": "2023-05-04T13:10:23-05:00"
                        }
                    ]
                },
                "deceasedBoolean": false,
                "address": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xad-address",
                                "extension": [
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/sad-address-line",
                                        "extension": [
                                            {
                                                "url": "SAD.1",
                                                "valueString": "555 STATE HIGHWAY 13"
                                            }
                                        ]
                                    },
                                    {
                                        "url": "XAD.7",
                                        "valueCode": "H"
                                    }
                                ]
                            }
                        ],
                        "use": "home",
                        "line": [
                            "555 STATE HIGHWAY 13"
                        ],
                        "city": "DEER CREEK",
                        "district": "OTTER TAIL",
                        "state": "MN",
                        "postalCode": "56527-9657",
                        "country": "USA"
                    }
                ],
                "multipleBirthInteger": 1
            }
        },
        {
            "fullUrl": "Organization/1713991686376793169.d63d39d1-bd81-4180-a007-1a963547ff8d",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686376793169.d63d39d1-bd81-4180-a007-1a963547ff8d",
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.1"
                            }
                        ],
                        "value": "CRPMRN"
                    }
                ]
            }
        },
        {
            "fullUrl": "ServiceRequest/1713991686444544794.4cd34f9e-a7da-489c-8c3a-bd8c3edfaf67",
            "resource": {
                "resourceType": "ServiceRequest",
                "id": "1713991686444544794.4cd34f9e-a7da-489c-8c3a-bd8c3edfaf67",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/business-event",
                        "valueCode": "NW"
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/business-event",
                        "valueString": "20230506052913-0500"
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/orc-common-order",
                        "extension": [
                            {
                                "url": "orc-21-ordering-facility-name",
                                "valueReference": {
                                    "reference": "Organization/1713991686439304395.dadd5b53-5f0e-4ca5-8e58-d6109d26f5ee"
                                }
                            },
                            {
                                "url": "orc-21-ordering-facility-name",
                                "valueReference": {
                                    "reference": "Organization/1713991686440767640.8871e10e-767a-46a3-81d3-150c106697f7"
                                }
                            },
                            {
                                "url": "orc-12-ordering-provider",
                                "valueReference": {
                                    "reference": "Practitioner/1713991686442730297.157adf6e-ef27-4065-a895-18dfd561d19e"
                                }
                            }
                        ]
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/obr-observation-request",
                        "extension": [
                            {
                                "url": "OBR.2",
                                "valueIdentifier": {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                            "extension": [
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                                    "valueString": "EPIC"
                                                },
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                                                    "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                                                },
                                                {
                                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                                                    "valueCode": "ISO"
                                                }
                                            ]
                                        }
                                    ],
                                    "value": "421832901"
                                }
                            }
                        ]
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "ORC.2"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                "extension": [
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                        "valueString": "EPIC"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                                        "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                                        "valueCode": "ISO"
                                    }
                                ]
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                    "code": "PLAC"
                                }
                            ]
                        },
                        "value": "{{placer_order_id}}"
                    }
                ],
                "status": "unknown",
                "subject": {
                    "reference": "Patient/1713991686420540992.4160b099-3871-449c-9e90-dadeb21100e7"
                },
                "authoredOn": "2023-05-06T05:29:13-05:00",
                "_authoredOn": {
                    "extension": [
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
                            "valueString": "20230506052913-0500"
                        }
                    ]
                },
                "requester": {
                    "reference": "PractitionerRole/1713991686430248172.048e0c37-4095-440f-994b-5cd80bf34c69"
                }
            }
        },
        {
            "fullUrl": "Organization/1713991686430978000.29e1ded0-6428-44af-8e58-0eeb2bea06af",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686430978000.29e1ded0-6428-44af-8e58-0eeb2bea06af",
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.1"
                            }
                        ],
                        "value": "NPI"
                    }
                ]
            }
        },
        {
            "fullUrl": "Practitioner/1713991686433173618.77eeecb0-0bad-4feb-8c05-d06d06735c90",
            "resource": {
                "resourceType": "Practitioner",
                "id": "1713991686433173618.77eeecb0-0bad-4feb-8c05-d06d06735c90",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                "valueString": "NPI"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                            }
                        ]
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
                        "extension": [
                            {
                                "url": "XCN.3",
                                "valueString": "JANE"
                            },
                            {
                                "url": "XCN.10",
                                "valueString": "L"
                            }
                        ]
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        "valueString": "ORC.12"
                    }
                ],
                "identifier": [
                    {
                        "type": {
                            "coding": [
                                {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/codeable-concept-id",
                                            "valueBoolean": true
                                        }
                                    ],
                                    "code": "NPI"
                                }
                            ]
                        },
                        "value": "1265136360",
                        "assigner": {
                            "reference": "Organization/1713991686430978000.29e1ded0-6428-44af-8e58-0eeb2bea06af"
                        }
                    }
                ],
                "name": [
                    {
                        "use": "official",
                        "family": "JONES",
                        "given": [
                            "JANE"
                        ]
                    }
                ]
            }
        },
        {
            "fullUrl": "Organization/1713991686435100196.d7b35c8a-94d2-492f-9010-728a69b55a92",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686435100196.d7b35c8a-94d2-492f-9010-728a69b55a92",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
                        "valueCoding": {
                            "extension": [
                                {
                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                                    "valueCodeableConcept": {
                                        "extension": [
                                            {
                                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                                "valueString": "XON.2"
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
                        "extension": [
                            {
                                "url": "XON.10",
                                "valueString": "1043269798"
                            }
                        ]
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                "extension": [
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                        "valueString": "CMS"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                                    }
                                ]
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                                            "valueString": "identifier"
                                        }
                                    ],
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                    "code": "NPI"
                                }
                            ]
                        },
                        "value": "1043269798"
                    }
                ],
                "name": "ST. CLOUD HOSPITAL"
            }
        },
        {
            "fullUrl": "PractitionerRole/1713991686430248172.048e0c37-4095-440f-994b-5cd80bf34c69",
            "resource": {
                "resourceType": "PractitionerRole",
                "id": "1713991686430248172.048e0c37-4095-440f-994b-5cd80bf34c69",
                "practitioner": {
                    "reference": "Practitioner/1713991686433173618.77eeecb0-0bad-4feb-8c05-d06d06735c90"
                },
                "organization": {
                    "reference": "Organization/1713991686435100196.d7b35c8a-94d2-492f-9010-728a69b55a92"
                }
            }
        },
        {
            "fullUrl": "Organization/1713991686439304395.dadd5b53-5f0e-4ca5-8e58-d6109d26f5ee",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686439304395.dadd5b53-5f0e-4ca5-8e58-d6109d26f5ee",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
                        "valueCoding": {
                            "extension": [
                                {
                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                                    "valueCodeableConcept": {
                                        "extension": [
                                            {
                                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                                "valueString": "XON.2"
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
                        "extension": [
                            {
                                "url": "XON.10",
                                "valueString": "1043269798"
                            }
                        ]
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                "extension": [
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                        "valueString": "CMS"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                                    }
                                ]
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                                            "valueString": "identifier"
                                        }
                                    ],
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                    "code": "NPI"
                                }
                            ]
                        },
                        "value": "1043269798"
                    }
                ],
                "name": "ST. CLOUD HOSPITAL"
            }
        },
        {
            "fullUrl": "Organization/1713991686440767640.8871e10e-767a-46a3-81d3-150c106697f7",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686440767640.8871e10e-767a-46a3-81d3-150c106697f7",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
                        "valueCoding": {
                            "extension": [
                                {
                                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                                    "valueCodeableConcept": {
                                        "extension": [
                                            {
                                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                                "valueString": "XON.2"
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
                        "extension": [
                            {
                                "url": "XON.10",
                                "valueString": "739"
                            }
                        ]
                    }
                ],
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                                "extension": [
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                        "valueString": "MN Public Health Lab"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                                    },
                                    {
                                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                                    }
                                ]
                            }
                        ],
                        "type": {
                            "coding": [
                                {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                                            "valueString": "identifier"
                                        }
                                    ],
                                    "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                                    "code": "Submitter ID"
                                }
                            ]
                        },
                        "value": "739"
                    }
                ],
                "name": "ST. CLOUD HOSPITAL"
            }
        },
        {
            "fullUrl": "Organization/1713991686441504208.f7b9b8ec-da49-4154-9290-0a8df47fbc4d",
            "resource": {
                "resourceType": "Organization",
                "id": "1713991686441504208.f7b9b8ec-da49-4154-9290-0a8df47fbc4d",
                "identifier": [
                    {
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                                "valueString": "HD.1"
                            }
                        ],
                        "value": "NPI"
                    }
                ]
            }
        },
        {
            "fullUrl": "Practitioner/1713991686442730297.157adf6e-ef27-4065-a895-18dfd561d19e",
            "resource": {
                "resourceType": "Practitioner",
                "id": "1713991686442730297.157adf6e-ef27-4065-a895-18dfd561d19e",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                        "extension": [
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                                "valueString": "NPI"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                            },
                            {
                                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                            }
                        ]
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
                        "extension": [
                            {
                                "url": "XCN.3",
                                "valueString": "JANE"
                            },
                            {
                                "url": "XCN.10",
                                "valueString": "L"
                            }
                        ]
                    }
                ],
                "identifier": [
                    {
                        "type": {
                            "coding": [
                                {
                                    "extension": [
                                        {
                                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/codeable-concept-id",
                                            "valueBoolean": true
                                        }
                                    ],
                                    "code": "NPI"
                                }
                            ]
                        },
                        "value": "1265136360",
                        "assigner": {
                            "reference": "Organization/1713991686441504208.f7b9b8ec-da49-4154-9290-0a8df47fbc4d"
                        }
                    }
                ],
                "name": [
                    {
                        "use": "official",
                        "family": "JONES",
                        "given": [
                            "JANE"
                        ]
                    }
                ]
            }
        },
        {
            "fullUrl": "Observation/1713991686770356478.1910e6f0-c091-4478-a7f6-ede1f3711b9f",
            "resource": {
                "resourceType": "Observation",
                "id": "1713991686770356478.1910e6f0-c091-4478-a7f6-ede1f3711b9f",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/sub-id",
                        "valueString": "1"
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/observation-sub-type",
                        "valueCode": "AOE"
                    },
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/obx-observation",
                        "extension": [
                            {
                                "url": "OBX.2",
                                "valueId": "NM"
                            },
                            {
                                "url": "OBX.6"
                            },
                            {
                                "url": "OBX.11",
                                "valueString": "O"
                            }
                        ]
                    }
                ],
                "status": "unknown",
                "subject": {
                    "reference": "Patient/1713991686420540992.4160b099-3871-449c-9e90-dadeb21100e7"
                },
                "effectiveDateTime": "2023-05-06T05:00:00-05:00",
                "_effectiveDateTime": {
                    "extension": [
                        {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
                            "valueString": "20230506050000-0500"
                        }
                    ]
                },
                "valueQuantity": {
                    "value": 1769.859285,
                    "unit": "gram",
                    "system": "UCUM",
                    "code": "g"
                }
            }
        },
        {
            "fullUrl": "Specimen/1713991686845404556.ccf23f10-1abe-4090-aa7c-c91dd0988c22",
            "resource": {
                "resourceType": "Specimen",
                "id": "1713991686845404556.ccf23f10-1abe-4090-aa7c-c91dd0988c22",
                "extension": [
                    {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Segment",
                        "valueString": "SPM"
                    }
                ]
            }
        }
    ]
}
    """


def get_result_fhir_message():
    # read the sample request body for the results endpoint
    return """{
  "resourceType": "Bundle",
  "id": "1708643329059388284.3eb8aca3-48fa-48e6-8d24-249f96d190f4",
  "meta": {
    "lastUpdated": "2024-02-22T23:08:49.065+00:00"
  },
  "identifier": {
    "system": "https://reportstream.cdc.gov/prime-router",
    "value": "20230607002849_0365"
  },
  "type": "message",
  "timestamp": "2023-06-07T00:28:49.000+00:00",
  "entry": [
    {
      "fullUrl": "MessageHeader/28e642fa-b0e7-3bc5-8084-293e36792f05",
      "resource": {
        "resourceType": "MessageHeader",
        "id": "28e642fa-b0e7-3bc5-8084-293e36792f05",
        "meta": {
          "tag": [
            {
              "system": "http://terminology.hl7.org/CodeSystem/v2-0103",
              "code": "P"
            }
          ]
        },
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/msh-message-header",
            "extension": [
              {
                "url": "MSH.7",
                "valueString": "20230606192849-0500"
              },
              {
                "url": "MSH.15",
                "valueString": "AL"
              },
              {
                "url": "MSH.16",
                "valueString": "AL"
              },
              {
                "url": "MSH.21",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.113883.9.195.3.4"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "LRI_NG_FRN_PROFILE"
                }
              },
              {
                "url": "MSH.21",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.113883.9.195.3.6"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "LRI_NDBS_COMPONENT"
                }
              },
              {
                "url": "MSH.21",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.113883.9.81"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "LAB_PRN_Component"
                }
              },
              {
                "url": "MSH.21",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.113883.9.22"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "LAB_TO_COMPONENT"
                }
              }
            ]
          }
        ],
        "eventCoding": {
          "system": "http://terminology.hl7.org/CodeSystem/v2-0003",
          "code": "R01",
          "display": "ORU^R01^ORU_R01"
        },
        "destination": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                "valueString": "ISO"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "MSH.5"
              }
            ],
            "name": "Epic",
            "endpoint": "urn:oid:1.2.840.114350.1.13.145.2.7.2.695071",
            "receiver": {
              "reference": "Organization/1708643329096405931.3df374db-c008-44a2-aa83-36e9b09a33ad"
            }
          }
        ],
        "sender": {
          "reference": "Organization/1708643329084338823.57892b53-2a11-43db-8ec9-97f8db64a32f"
        },
        "source": {
          "extension": [
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
              "valueString": "Natus"
            },
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
              "valueString": "natus.health.state.mn.us"
            },
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
              "valueString": "DNS"
            },
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
              "valueString": "MSH.3"
            }
          ],
          "endpoint": "urn:dns:natus.health.state.mn.us"
        }
      }
    },
    {
      "fullUrl": "Organization/1708643329084338823.57892b53-2a11-43db-8ec9-97f8db64a32f",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329084338823.57892b53-2a11-43db-8ec9-97f8db64a32f",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "MN Public Health Lab"
          },
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.2,HD.3"
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0301",
                  "code": "ISO"
                }
              ]
            },
            "system": "urn:ietf:rfc:3986",
            "value": "2.16.840.1.114222.4.1.10080"
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329096405931.3df374db-c008-44a2-aa83-36e9b09a33ad",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329096405931.3df374db-c008-44a2-aa83-36e9b09a33ad",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
            "valueString": "MSH.6"
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "Centracare"
          },
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.2,HD.3"
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0301",
                  "code": "DNS"
                }
              ]
            },
            "value": "centracare.com"
          }
        ]
      }
    },
    {
      "fullUrl": "Provenance/1708643329181039838.5d4b6ba7-fe37-4bde-9e17-5cb502c08d08",
      "resource": {
        "resourceType": "Provenance",
        "id": "1708643329181039838.5d4b6ba7-fe37-4bde-9e17-5cb502c08d08",
        "target": [
          {
            "reference": "MessageHeader/28e642fa-b0e7-3bc5-8084-293e36792f05"
          },
          {
            "reference": "DiagnosticReport/1708643329631593922.78f28950-7a6e-4fcb-8980-e1842b578708"
          }
        ],
        "recorded": "2023-06-06T19:28:49-05:00",
        "activity": {
          "coding": [
            {
              "display": "ORU^R01^ORU_R01"
            }
          ]
        },
        "agent": [
          {
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/provenance-participant-type",
                  "code": "author"
                }
              ]
            },
            "who": {
              "reference": "Organization/1708643329175239534.8db4b5dd-ea1c-42e7-bcd9-c61e1c4ca158"
            }
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329175239534.8db4b5dd-ea1c-42e7-bcd9-c61e1c4ca158",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329175239534.8db4b5dd-ea1c-42e7-bcd9-c61e1c4ca158",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "MN Public Health Lab"
          },
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.2,HD.3"
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0301",
                  "code": "ISO"
                }
              ]
            },
            "system": "urn:ietf:rfc:3986",
            "value": "2.16.840.1.114222.4.1.10080"
          }
        ]
      }
    },
    {
      "fullUrl": "Provenance/1708643329194237432.5dc530b3-ed05-4738-91e1-188074ce4af9",
      "resource": {
        "resourceType": "Provenance",
        "id": "1708643329194237432.5dc530b3-ed05-4738-91e1-188074ce4af9",
        "recorded": "2024-02-22T23:08:49Z",
        "policy": [
          "http://hl7.org/fhir/uv/v2mappings/message-oru-r01-to-bundle"
        ],
        "activity": {
          "coding": [
            {
              "code": "v2-FHIR transformation"
            }
          ]
        },
        "agent": [
          {
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/provenance-participant-type",
                  "code": "assembler"
                }
              ]
            },
            "who": {
              "reference": "Organization/1708643329192128256.d81d233e-da75-4eb1-a636-e0818b307ee0"
            }
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329192128256.d81d233e-da75-4eb1-a636-e0818b307ee0",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329192128256.d81d233e-da75-4eb1-a636-e0818b307ee0",
        "identifier": [
          {
            "value": "CDC PRIME - Atlanta"
          },
          {
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0301"
                }
              ]
            },
            "system": "urn:ietf:rfc:3986",
            "value": "2.16.840.1.114222.4.1.237821"
          }
        ]
      }
    },
    {
      "fullUrl": "Patient/1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6",
      "resource": {
        "resourceType": "Patient",
        "id": "1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/pid-patient",
            "extension": [
              {
                "url": "PID.30",
                "valueString": "N"
              }
            ]
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cx-identifier",
                "extension": [
                  {
                    "url": "CX.5",
                    "valueString": "MR"
                  }
                ]
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "PID.3"
              }
            ],
            "type": {
              "coding": [
                {
                  "code": "MR"
                }
              ]
            },
            "value": "11102779",
            "assigner": {
              "reference": "Organization/1708643329205656925.de11953b-df2c-4b5c-a8f5-4882ae39faa5"
            }
          }
        ],
        "name": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xpn-human-name",
                "extension": [
                  {
                    "url": "XPN.2",
                    "valueString": "BB SARAH"
                  },
                  {
                    "url": "XPN.6",
                    "valueString": "L"
                  }
                ]
              }
            ],
            "text": "TestValue",
            "family": "SMITH",
            "given": [
              "BB SARAH"
            ],
            "suffix": [
              "L"
            ]
          }
        ],
        "birthDate": "2023-05-04",
        "_birthDate": {
          "extension": [
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
              "valueString": "20230504131000"
            },
            {
              "url": "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
              "valueDateTime": "2023-05-04T13:10:00Z"
            }
          ]
        },
        "deceasedBoolean": false,
        "address": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xad-address",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/sad-address-line",
                    "extension": [
                      {
                        "url": "SAD.1",
                        "valueString": "555 STATE HIGHWAY 13"
                      }
                    ]
                  },
                  {
                    "url": "XAD.7",
                    "valueCode": "H"
                  }
                ]
              }
            ],
            "use": "home",
            "line": [
              "555 STATE HIGHWAY 13"
            ],
            "city": "DEER CREEK",
            "state": "MN",
            "postalCode": "565279657"
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329205656925.de11953b-df2c-4b5c-a8f5-4882ae39faa5",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329205656925.de11953b-df2c-4b5c-a8f5-4882ae39faa5",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "CRPMRN"
          }
        ]
      }
    },
    {
      "fullUrl": "Provenance/1708643329283292699.668bb1d4-3c6f-4a04-9085-b16177a586f0",
      "resource": {
        "resourceType": "Provenance",
        "id": "1708643329283292699.668bb1d4-3c6f-4a04-9085-b16177a586f0",
        "target": [
          {
            "reference": "Patient/1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6"
          }
        ],
        "recorded": "2024-02-22T23:08:49Z",
        "activity": {
          "coding": [
            {
              "system": "https://terminology.hl7.org/CodeSystem/v3-DataOperation",
              "code": "UPDATE"
            }
          ]
        }
      }
    },
    {
      "fullUrl": "Observation/1708643329337615661.69d9df30-4fa0-4d5d-9be3-eb3c441349d2",
      "resource": {
        "resourceType": "Observation",
        "id": "1708643329337615661.69d9df30-4fa0-4d5d-9be3-eb3c441349d2",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/analysis-date-time",
            "valueDateTime": "2023-06-06T19:16:13Z",
            "_valueDateTime": {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
                  "valueString": "20230606191613"
                }
              ]
            }
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/observation-sub-type",
            "valueCode": "UNSP"
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/obx-observation",
            "extension": [
              {
                "url": "OBX.2",
                "valueId": "CWE"
              },
              {
                "url": "OBX.11",
                "valueString": "F"
              }
            ]
          }
        ],
        "status": "final",
        "code": {
          "coding": [
            {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueString": "coding"
                },
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                  "valueString": "LN"
                }
              ],
              "system": "http://loinc.org",
              "code": "57718-9",
              "display": "Sample quality of Dried blood spot"
            }
          ]
        },
        "subject": {
          "reference": "Patient/1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6"
        },
        "effectiveDateTime": "2023-06-03T04:50:00Z",
        "_effectiveDateTime": {
          "extension": [
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
              "valueString": "20230603045000"
            }
          ]
        },
        "performer": [
          {
            "reference": "PractitionerRole/1708643329346346926.5862ad7a-6aee-446b-888a-456b0b025016"
          }
        ],
        "valueCodeableConcept": {
          "coding": [
            {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueString": "coding"
                },
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                  "valueString": "LN"
                }
              ],
              "system": "http://loinc.org",
              "code": "LA12432-3",
              "display": "Acceptable"
            }
          ]
        }
      }
    },
    {
      "fullUrl": "Practitioner/1708643329366242745.3a0e80f3-82fc-4d17-97e9-0b2035adce46",
      "resource": {
        "resourceType": "Practitioner",
        "id": "1708643329366242745.3a0e80f3-82fc-4d17-97e9-0b2035adce46",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
            "extension": [
              {
                "url": "XCN.3",
                "valueString": "Marie"
              }
            ]
          }
        ],
        "name": [
          {
            "family": "Miller",
            "given": [
              "Marie"
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329385366275.d605419a-f07c-4149-8b70-32f577083612",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329385366275.d605419a-f07c-4149-8b70-32f577083612",
        "name": "MDH Newborn Screening Laboratory",
        "address": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xad-address",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/sad-address-line",
                    "extension": [
                      {
                        "url": "SAD.1",
                        "valueString": "601 Robert St N"
                      }
                    ]
                  }
                ]
              }
            ],
            "line": [
              "601 Robert St N"
            ],
            "city": "St. Paul",
            "state": "MN",
            "postalCode": "55155"
          }
        ]
      }
    },
    {
      "fullUrl": "PractitionerRole/1708643329346346926.5862ad7a-6aee-446b-888a-456b0b025016",
      "resource": {
        "resourceType": "PractitionerRole",
        "id": "1708643329346346926.5862ad7a-6aee-446b-888a-456b0b025016",
        "practitioner": {
          "reference": "Practitioner/1708643329366242745.3a0e80f3-82fc-4d17-97e9-0b2035adce46"
        },
        "organization": {
          "reference": "Organization/1708643329385366275.d605419a-f07c-4149-8b70-32f577083612"
        },
        "code": [
          {
            "coding": [
              {
                "system": "http://terminology.hl7.org/CodeSystem/v2-0912",
                "code": "MDIR"
              }
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "Specimen/1708643329426115363.caf8de32-9ff6-4fcd-a4dd-6b41c7eec275",
      "resource": {
        "resourceType": "Specimen",
        "id": "1708643329426115363.caf8de32-9ff6-4fcd-a4dd-6b41c7eec275",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Segment",
            "valueString": "OBR"
          }
        ]
      }
    },
    {
      "fullUrl": "ServiceRequest/1708643329612626874.6a767bc5-6fa2-488a-bf90-a3ecbc814fed",
      "resource": {
        "resourceType": "ServiceRequest",
        "id": "1708643329612626874.6a767bc5-6fa2-488a-bf90-a3ecbc814fed",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/business-event",
            "valueCode": "RE"
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/orc-common-order",
            "extension": [
              {
                "url": "orc-21-ordering-facility-name",
                "valueReference": {
                  "reference": "Organization/1708643329532919335.1c95b0f6-e401-4ae7-bf8c-9d698bd46a57"
                }
              },
              {
                "url": "orc-21-ordering-facility-name",
                "valueReference": {
                  "reference": "Organization/1708643329552456753.88e4eb0d-dece-4579-a8a1-a4014f4bcda5"
                }
              },
              {
                "url": "ORC.31",
                "valueCodeableConcept": {
                  "coding": [
                    {
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                          "valueString": "coding"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                          "valueString": "LN"
                        }
                      ],
                      "system": "http://loinc.org",
                      "code": "54089-8",
                      "display": "Newborn screening panel American Health Information Community (AHIC)"
                    }
                  ]
                }
              },
              {
                "url": "orc-12-ordering-provider",
                "valueReference": {
                  "reference": "Practitioner/1708643329577508774.3caa1170-d0a8-482d-9a11-ea9d568db749"
                }
              }
            ]
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/obr-observation-request",
            "extension": [
              {
                "url": "OBR.2",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                          "valueString": "EPIC"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "423787478"
                }
              },
              {
                "url": "OBR.3",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                          "valueString": "MN Public Health Lab"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.114222.4.1.10080"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueCode": "ISO"
                        }
                      ]
                    }
                  ],
                  "value": "20231561137"
                }
              },
              {
                "url": "OBR.22",
                "valueString": "20230606191613"
              },
              {
                "url": "OBR.29",
                "valueIdentifier": {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                      "valueString": "EPIC"
                    },
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                      "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                    },
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                      "valueString": "ISO"
                    },
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/filler-assigned-identifier",
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/entity-identifier",
                          "valueString": "20231561137"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                          "valueString": "MN Public Health Lab"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                          "valueString": "2.16.840.1.114222.4.1.10080"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                          "valueString": "ISO"
                        }
                      ]
                    }
                  ],
                  "type": {
                    "coding": [
                      {
                        "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                        "code": "PGN"
                      }
                    ]
                  },
                  "value": "423787478"
                }
              },
              {
                "url": "OBR.50",
                "valueCodeableConcept": {
                  "coding": [
                    {
                      "extension": [
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                          "valueString": "coding"
                        },
                        {
                          "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                          "valueString": "LN"
                        }
                      ],
                      "system": "http://loinc.org",
                      "code": "54089-8",
                      "display": "Newborn screening panel American Health Information Community (AHIC)"
                    }
                  ]
                }
              },
              {
                "url": "OBR.11",
                "valueString": "O"
              },
              {
                "url": "OBR.16",
                "valueReference": {
                  "reference": "Practitioner/1708643329606988679.6374ee92-a08b-4cb6-97c6-2b09442476ff"
                }
              }
            ]
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "ORC.2"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "EPIC"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                    "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                    "valueCode": "ISO"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "PLAC"
                }
              ]
            },
            "value": "{{placer_order_id}}"
          },
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "ORC.3"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "MN Public Health Lab"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                    "valueString": "2.16.840.1.114222.4.1.10080"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                    "valueCode": "ISO"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "FILL"
                }
              ]
            },
            "value": "20231561137"
          }
        ],
        "status": "unknown",
        "intent": "order",
        "code": {
          "coding": [
            {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueString": "coding"
                },
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                  "valueString": "LN"
                }
              ],
              "system": "http://loinc.org",
              "code": "57128-1",
              "display": "Newborn Screening Report summary panel"
            }
          ]
        },
        "subject": {
          "reference": "Patient/1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6"
        },
        "requester": {
          "reference": "PractitionerRole/1708643329436006371.45eabfd2-cdfc-4920-b10f-b05e1a6acab7"
        }
      }
    },
    {
      "fullUrl": "Organization/1708643329440493082.2d0201f2-7497-4f14-a56a-8be814df6cce",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329440493082.2d0201f2-7497-4f14-a56a-8be814df6cce",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "NPI"
          }
        ]
      }
    },
    {
      "fullUrl": "Practitioner/1708643329457407861.c164c0c9-903e-486c-81bb-f3a697da095f",
      "resource": {
        "resourceType": "Practitioner",
        "id": "1708643329457407861.c164c0c9-903e-486c-81bb-f3a697da095f",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                "valueString": "NPI"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
              }
            ]
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
            "extension": [
              {
                "url": "XCN.3",
                "valueString": "SAMANTHA"
              },
              {
                "url": "XCN.10",
                "valueString": "L"
              }
            ]
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
            "valueString": "ORC.12"
          }
        ],
        "identifier": [
          {
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/codeable-concept-id",
                      "valueBoolean": true
                    }
                  ],
                  "code": "NPI"
                }
              ]
            },
            "value": "1174911127",
            "assigner": {
              "reference": "Organization/1708643329440493082.2d0201f2-7497-4f14-a56a-8be814df6cce"
            }
          }
        ],
        "name": [
          {
            "use": "official",
            "family": "SMITH",
            "given": [
              "SAMANTHA"
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329482488611.58a0bd6a-0d77-4d70-a95f-6028d43bb644",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329482488611.58a0bd6a-0d77-4d70-a95f-6028d43bb644",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
            "valueCoding": {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueCodeableConcept": {
                    "extension": [
                      {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        "valueString": "XON.2"
                      }
                    ],
                    "coding": [
                      {
                        "extension": [
                          {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                            "valueString": "coding"
                          }
                        ],
                        "code": "L"
                      }
                    ]
                  }
                }
              ],
              "code": "L"
            }
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
            "extension": [
              {
                "url": "XON.10",
                "valueString": "1043269798"
              }
            ]
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "CMS"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                      "valueString": "identifier"
                    }
                  ],
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "NPI"
                }
              ]
            },
            "value": "1043269798"
          }
        ],
        "name": "ST. CLOUD HOSPITAL",
        "telecom": [
          {
            "_system": {
              "extension": [
                {
                  "url": "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
                  "valueCode": "unknown"
                }
              ]
            }
          }
        ]
      }
    },
    {
      "fullUrl": "PractitionerRole/1708643329436006371.45eabfd2-cdfc-4920-b10f-b05e1a6acab7",
      "resource": {
        "resourceType": "PractitionerRole",
        "id": "1708643329436006371.45eabfd2-cdfc-4920-b10f-b05e1a6acab7",
        "practitioner": {
          "reference": "Practitioner/1708643329457407861.c164c0c9-903e-486c-81bb-f3a697da095f"
        },
        "organization": {
          "reference": "Organization/1708643329482488611.58a0bd6a-0d77-4d70-a95f-6028d43bb644"
        }
      }
    },
    {
      "fullUrl": "Organization/1708643329532919335.1c95b0f6-e401-4ae7-bf8c-9d698bd46a57",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329532919335.1c95b0f6-e401-4ae7-bf8c-9d698bd46a57",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
            "valueCoding": {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueCodeableConcept": {
                    "extension": [
                      {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        "valueString": "XON.2"
                      }
                    ],
                    "coding": [
                      {
                        "extension": [
                          {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                            "valueString": "coding"
                          }
                        ],
                        "code": "L"
                      }
                    ]
                  }
                }
              ],
              "code": "L"
            }
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
            "extension": [
              {
                "url": "XON.10",
                "valueString": "1043269798"
              }
            ]
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "CMS"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                      "valueString": "identifier"
                    }
                  ],
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "NPI"
                }
              ]
            },
            "value": "1043269798"
          }
        ],
        "name": "ST. CLOUD HOSPITAL"
      }
    },
    {
      "fullUrl": "Organization/1708643329552456753.88e4eb0d-dece-4579-a8a1-a4014f4bcda5",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329552456753.88e4eb0d-dece-4579-a8a1-a4014f4bcda5",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/organization-name-type",
            "valueCoding": {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueCodeableConcept": {
                    "extension": [
                      {
                        "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                        "valueString": "XON.2"
                      }
                    ],
                    "coding": [
                      {
                        "extension": [
                          {
                            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                            "valueString": "coding"
                          }
                        ],
                        "code": "L"
                      }
                    ]
                  }
                }
              ],
              "code": "L"
            }
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xon-organization",
            "extension": [
              {
                "url": "XON.10",
                "valueString": "739"
              }
            ]
          }
        ],
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "MN Public Health Lab"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/code-index-name",
                      "valueString": "identifier"
                    }
                  ],
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "Submitter ID"
                }
              ]
            },
            "value": "739"
          }
        ],
        "name": "ST. CLOUD HOSPITAL"
      }
    },
    {
      "fullUrl": "Organization/1708643329559325319.b84e8226-acee-487e-8e81-afb8bd63d91e",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329559325319.b84e8226-acee-487e-8e81-afb8bd63d91e",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "NPI"
          }
        ]
      }
    },
    {
      "fullUrl": "Practitioner/1708643329577508774.3caa1170-d0a8-482d-9a11-ea9d568db749",
      "resource": {
        "resourceType": "Practitioner",
        "id": "1708643329577508774.3caa1170-d0a8-482d-9a11-ea9d568db749",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                "valueString": "NPI"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
              }
            ]
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
            "extension": [
              {
                "url": "XCN.3",
                "valueString": "SAMANTHA"
              },
              {
                "url": "XCN.10",
                "valueString": "L"
              }
            ]
          }
        ],
        "identifier": [
          {
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/codeable-concept-id",
                      "valueBoolean": true
                    }
                  ],
                  "code": "NPI"
                }
              ]
            },
            "value": "1174911127",
            "assigner": {
              "reference": "Organization/1708643329559325319.b84e8226-acee-487e-8e81-afb8bd63d91e"
            }
          }
        ],
        "name": [
          {
            "use": "official",
            "family": "SMITH",
            "given": [
              "SAMANTHA"
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "Organization/1708643329596847368.93f52b88-0146-4868-a272-86cd247ef0b9",
      "resource": {
        "resourceType": "Organization",
        "id": "1708643329596847368.93f52b88-0146-4868-a272-86cd247ef0b9",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "HD.1"
              }
            ],
            "value": "NPI"
          }
        ]
      }
    },
    {
      "fullUrl": "Practitioner/1708643329606988679.6374ee92-a08b-4cb6-97c6-2b09442476ff",
      "resource": {
        "resourceType": "Practitioner",
        "id": "1708643329606988679.6374ee92-a08b-4cb6-97c6-2b09442476ff",
        "extension": [
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                "valueString": "NPI"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-unknown-type"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type"
              }
            ]
          },
          {
            "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/xcn-practitioner",
            "extension": [
              {
                "url": "XCN.3",
                "valueString": "SAMANTHA"
              },
              {
                "url": "XCN.10",
                "valueString": "L"
              }
            ]
          }
        ],
        "identifier": [
          {
            "type": {
              "coding": [
                {
                  "extension": [
                    {
                      "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/codeable-concept-id",
                      "valueBoolean": true
                    }
                  ],
                  "code": "NPI"
                }
              ]
            },
            "value": "1174911127",
            "assigner": {
              "reference": "Organization/1708643329596847368.93f52b88-0146-4868-a272-86cd247ef0b9"
            }
          }
        ],
        "name": [
          {
            "use": "official",
            "family": "SMITH",
            "given": [
              "SAMANTHA"
            ]
          }
        ]
      }
    },
    {
      "fullUrl": "DiagnosticReport/1708643329631593922.78f28950-7a6e-4fcb-8980-e1842b578708",
      "resource": {
        "resourceType": "DiagnosticReport",
        "id": "1708643329631593922.78f28950-7a6e-4fcb-8980-e1842b578708",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field",
                "valueString": "ORC.2"
              },
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "EPIC"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                    "valueString": "1.2.840.114350.1.13.145.2.7.2.695071"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                    "valueCode": "ISO"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "PLAC"
                }
              ]
            },
            "value": "423787478"
          },
          {
            "extension": [
              {
                "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/assigning-authority",
                "extension": [
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id",
                    "valueString": "MN Public Health Lab"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id",
                    "valueString": "2.16.840.1.114222.4.1.10080"
                  },
                  {
                    "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type",
                    "valueCode": "ISO"
                  }
                ]
              }
            ],
            "type": {
              "coding": [
                {
                  "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "code": "FILL"
                }
              ]
            },
            "value": "20231561137"
          }
        ],
        "basedOn": [
          {
            "reference": "ServiceRequest/1708643329612626874.6a767bc5-6fa2-488a-bf90-a3ecbc814fed"
          }
        ],
        "status": "final",
        "code": {
          "coding": [
            {
              "extension": [
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding",
                  "valueString": "coding"
                },
                {
                  "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/cwe-coding-system",
                  "valueString": "LN"
                }
              ],
              "system": "http://loinc.org",
              "code": "57128-1",
              "display": "Newborn Screening Report summary panel"
            }
          ]
        },
        "subject": {
          "reference": "Patient/1708643329268871344.12d74d82-41ed-4380-a4e0-d276570f04e6"
        },
        "effectiveDateTime": "2023-06-03T04:50:00Z",
        "_effectiveDateTime": {
          "extension": [
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
              "valueString": "20230603045000"
            }
          ]
        },
        "issued": "2023-06-06T19:16:13Z",
        "_issued": {
          "extension": [
            {
              "url": "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2-date-time",
              "valueString": "20230606191613"
            }
          ]
        },
        "specimen": [
          {
            "reference": "Specimen/1708643329426115363.caf8de32-9ff6-4fcd-a4dd-6b41c7eec275"
          }
        ],
        "result": [
          {
            "reference": "Observation/1708643329337615661.69d9df30-4fa0-4d5d-9be3-eb3c441349d2"
          }
        ]
      }
    }
  ]
}
    """
