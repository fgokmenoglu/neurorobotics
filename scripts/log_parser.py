import tkinter as tk
from tkinter import filedialog, ttk, messagebox
import csv
from datetime import datetime
import os

def display_data_in_window(parent, header, data):
    """
    Creates a new TOPLEVEL window to display the data.
    """
    win = tk.Toplevel(parent)
    win.title("KUKA Log Data Viewer")
    win.geometry("1200x700")

    frame = tk.Frame(win)
    frame.pack(pady=20, padx=20, fill='both', expand=True)

    tree = ttk.Treeview(frame, columns=header, show='headings')

    for col in header:
        tree.heading(col, text=col.replace('_', ' '))
        tree.column(col, width=150, anchor='center')

    for row in data:
        tree.insert("", "end", values=row)

    vsb = ttk.Scrollbar(frame, orient="vertical", command=tree.yview)
    vsb.pack(side='right', fill='y')
    tree.configure(yscrollcommand=vsb.set)

    hsb = ttk.Scrollbar(frame, orient="horizontal", command=tree.xview)
    hsb.pack(side='bottom', fill='x')
    tree.configure(xscrollcommand=hsb.set)

    tree.pack(side='left', fill='both', expand=True)

    parent.wait_window(win)

def parse_log_file(filepath):
    """
    Reads KUKA log data, calculates cumulative time, and prepares the data.
    """
    final_data = []
    final_header = []

    try:
        with open(filepath, 'r', newline='') as logfile:
            reader = csv.reader(logfile, delimiter='_')

            header = next(reader, None)
            if not header:
                return None, None

            final_header = header[:]
            final_header[0] = "Timestamp"
            final_header.insert(1, "Cumulative_Time_s")

            start_time_ms = 0
            is_first_row = True

            for row in reader:
                if len(row) > 0:
                    unix_timestamp_ms = int(row[0])

                    if is_first_row:
                        start_time_ms = unix_timestamp_ms
                        is_first_row = False

                    cumulative_time_s = (unix_timestamp_ms - start_time_ms) / 1000.0

                    dt_object = datetime.fromtimestamp(unix_timestamp_ms / 1000)
                    readable_timestamp = dt_object.strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]

                    final_row = row[:]
                    final_row[0] = readable_timestamp
                    final_row.insert(1, f"{cumulative_time_s:.3f}")
                    final_data.append(final_row)

    except (IOError, ValueError, IndexError) as e:
        messagebox.showerror("File Error", f"Could not read or parse the file.\n\nError: {e}")
        return None, None

    return final_header, final_data

def save_as_tab_separated(filepath, header, data):
    """
    Saves the parsed data to a new, clean tab-separated file,
    replacing commas with periods in all data points.
    """
    try:
        # --- MODIFIED: Pre-process the data to replace commas with periods ---
        processed_data = []
        for row in data:
            # Create a new row where each item has commas replaced by periods
            new_row = [str(item).replace(',', '.') for item in row]
            processed_data.append(new_row)

        with open(filepath, 'w', newline='') as f:
            writer = csv.writer(f, delimiter='\t')
            writer.writerow(header)
            # Write the newly processed data to the file
            writer.writerows(processed_data)
        messagebox.showinfo("Success", f"Successfully created data file at:\n{filepath}")
    except Exception as e:
        messagebox.showerror("Save Error", f"Could not save the file.\n\nError: {e}")

def main():
    """
    Main function to run the log parser.
    """
    root = tk.Tk()
    root.withdraw()

    file_path = filedialog.askopenfilename(
        parent=root,
        title="Select a KUKA Log File",
        filetypes=(("Log files", "*.csv *.txt"), ("All files", "*.*"))
    )

    if not file_path:
        root.destroy()
        return

    final_header, final_data = parse_log_file(file_path)

    if final_header and final_data:
        display_data_in_window(root, final_header, final_data)

        original_dir, original_filename = os.path.split(file_path)
        new_filename = os.path.splitext(original_filename)[0] + "_parsed.txt"
        save_path = os.path.join(original_dir, new_filename)

        save_as_tab_separated(save_path, final_header, final_data)

    root.destroy()

if __name__ == "__main__":
    main()
