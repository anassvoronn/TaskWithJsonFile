import json
import shutil
import uuid
import random

INPUT_FILE = "device_config.json"
OUTPUT_FILE = "device_config_generated.json"
BACKUP_FILE = "device_config_backup.json"

GENERATED_COUNT = 5_000_000

# -----------------------
# backup
# -----------------------
shutil.copy(INPUT_FILE, BACKUP_FILE)
print(f"Backup created: {BACKUP_FILE}")

# -----------------------
# enum
# -----------------------
HOST_TYPES = [
    "DOMAIN",
    "CHECKPOINT_NETWORK",
    "INTERNAL",
    "SECURITY_ZONE",
    "IDENTITY"
]


def random_host_type():
    return random.choices(HOST_TYPES, weights=[10, 20, 50, 10, 10])[0]


# -----------------------
# генерация host
# -----------------------
def generate_host(i):
    return {
        "id": str(uuid.uuid4()),
        "name": f"generated-host-{i}",
        "comment": "auto generated",
        "comments": "",
        "members": [],
        "ips": [
            f"10.{random.randint(0,255)}.{random.randint(0,255)}.{random.randint(1,254)}"
        ],
        "fqdn": f"host{i}.example.com",
        "is_negate": False,
        "type": random_host_type(),
        "additional_properties": {
            "original_name": f"gen-{i}",
            "broadcast": None,
            "nat-auto-rule": False,
            "global_level": False,
            "ckp_type": "host",
            "nat-target": None,
            "nat-ipv4": None,
            "nat-method": None,
            "location": "generated"
        }
    }


# -----------------------
# читаем исходный файл
# -----------------------
with open(INPUT_FILE, "r", encoding="utf-8") as f:
    data = json.load(f)

hosts_raw = data.get("hosts", {})

# -----------------------
# нормализация hosts → dict
# -----------------------
hosts = {}

if isinstance(hosts_raw, dict):
    # уже нормальная map
    hosts = hosts_raw

elif isinstance(hosts_raw, list):
    # если вдруг list → превращаем в map
    for h in hosts_raw:
        if isinstance(h, dict):
            key = h.get("name") or str(uuid.uuid4())
            hosts[key] = h
        else:
            key = str(h)
            hosts[key] = {
                "id": str(uuid.uuid4()),
                "name": key,
                "comment": "",
                "comments": "",
                "members": [],
                "ips": [key],
                "fqdn": None,
                "is_negate": False,
                "type": "INTERNAL",
                "additional_properties": {}
            }

print(f"Existing hosts: {len(hosts)}")

# -----------------------
# запись
# -----------------------
with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
    out.write("{\n")

    # все поля кроме hosts
    for key, value in data.items():
        if key != "hosts":
            out.write(f'"{key}": {json.dumps(value, ensure_ascii=False)},\n')

    # hosts как MAP
    out.write('"hosts": {\n')

    first = True

    # ---- старые ----
    for key, host in hosts.items():
        if not first:
            out.write(",\n")
        first = False

        out.write(f'"{key}": ')
        json.dump(host, out, ensure_ascii=False)

    # ---- новые ----
    for i in range(GENERATED_COUNT):
        new_key = f"generated-host-{i}"
        new_host = generate_host(i)

        if not first:
            out.write(",\n")
        first = False

        out.write(f'"{new_key}": ')
        json.dump(new_host, out, ensure_ascii=False)

        if i % 10000 == 0 and i != 0:
            print(f"Generated: {i}")

    out.write("\n}\n")
    out.write("}")

print(f"Done! File created: {OUTPUT_FILE}")