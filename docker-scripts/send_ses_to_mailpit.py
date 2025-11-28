#!/usr/bin/env python3
import os
import json
import base64
import smtplib
from email import message_from_bytes, policy
from email.message import EmailMessage

SES_DIR = "/tmp/localstack/state/ses"
MAILPIT_HOST = "mailpit"
MAILPIT_PORT = 1025

def forward_to_mailpit(subject, from_addr, to_addrs, attachments):
    msg = EmailMessage()
    msg["From"] = from_addr
    msg["To"] = ", ".join(to_addrs)
    msg["Subject"] = f"[FORWARDED] {subject}"
    msg.set_content("Forwarded from LocalStack SES simulation.\nAttachments included below.")

    for filename, data in attachments:
        maintype, subtype = "application", "octet-stream"
        msg.add_attachment(data, maintype=maintype, subtype=subtype, filename=filename)

    with smtplib.SMTP(MAILPIT_HOST, MAILPIT_PORT) as smtp:
        smtp.send_message(msg)
        print(f"Forwarded to Mailpit: {subject} ({len(attachments)} attachments)")

def process_ses_file(filepath):
    try:
        with open(filepath, "r") as f:
            content = json.load(f)

        raw = content.get("RawData")
        if not raw:
            print(f"No RawData in {filepath}")
            return

        raw_bytes = raw.encode("utf-8", errors="ignore")
        msg = message_from_bytes(raw_bytes, policy=policy.default)

        subject = msg.get("Subject", "(no subject)")
        from_addr = msg.get("From", "no-reply@lumen.local")
        to_addrs = [addr.strip() for addr in msg.get("To", "").split(",") if addr.strip()]

        print(f"\nProcessing email: {subject}")
        print(f"   From: {from_addr}")
        print(f"   To:   {to_addrs}")

        attachments = []
        if msg.is_multipart():
            for part in msg.iter_attachments():
                filename = part.get_filename()
                if not filename:
                    continue
                data = part.get_payload(decode=True)
                if data:
                    attachments.append((filename, data))
                    print(f"   ↳ Found attachment: {filename} ({len(data)} bytes)")
        else:
            print("No attachments found.")

        if attachments:
            forward_to_mailpit(subject, from_addr, to_addrs, attachments)
        else:
            print("No attachments to forward.")

    except Exception as e:
        print(f"Error processing {os.path.basename(filepath)}: {e}")

def main():
    print(f"=== Scanning {SES_DIR} for SES messages ===")
    if not os.path.isdir(SES_DIR):
        print("No SES folder found.")
        return

    files = [os.path.join(SES_DIR, f) for f in os.listdir(SES_DIR) if f.endswith(".json")]
    if not files:
        print("No SES messages found.")
        return

    for fpath in sorted(files):
        process_ses_file(fpath)

    print("\nDone. All emails forwarded to Mailpit (http://localhost:8025)")

if __name__ == "__main__":
    main()
