const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validPartyPayload(userId = ALICE_UID) {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    name: "Ahmad Wholesale Traders",
    phone: "+1555019283",
    role: "SUPPLIER",
    notes: "Monthly spice supplier",
    totalGivenCents: 15000,
    totalGotCents: 40000,
    netBalanceCents: -25000,
    entryCount: 2,
    lastActivitySummary: "Got $400.00 - Stock Purchase",
    dueDateMillis: 0,
    createdAt: now,
    updatedAt: now,
  };
}

function validEntryPayload(userId = ALICE_UID, partyId = "party_1") {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    partyId,
    partyName: "Ahmad Wholesale Traders",
    type: "GOT",
    amountCents: 40000,
    description: "20kg Basmati Rice & Spices",
    category: "Stock Purchase",
    paymentMode: "CREDIT",
    referenceNote: "INV-204",
    entryDateMillis: 1729380000000,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read or create parties", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("parties").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_1")
      .set(validPartyPayload(ALICE_UID))
  );
});

test("Authenticated user: can create and read their own party", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_1")
      .set(validPartyPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_1")
      .get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").get()
  );
});

test("Authenticated user: cannot read or write another user's parties", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("parties")
      .doc("bob_party")
      .set(validPartyPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("parties")
      .doc("bob_party")
      .get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("parties").get()
  );
});

test("Shadow update & balance invariant check: rejects mismatched netBalanceCents or ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_bad_balance")
      .set({
        ...validPartyPayload(ALICE_UID),
        netBalanceCents: 99999,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_ghost")
      .set({
        ...validPartyPayload(ALICE_UID),
        isAdmin: true,
      })
  );
});

test("Ledger entry requires parent party to exist and enforces schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("entries")
      .doc("entry_orphan")
      .set(validEntryPayload(ALICE_UID, "party_nonexistent"))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("parties")
      .doc("party_1")
      .set(validPartyPayload(ALICE_UID))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("entries")
      .doc("entry_1")
      .set(validEntryPayload(ALICE_UID, "party_1"))
  );
});
