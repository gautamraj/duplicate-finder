A simple duplicate file finder that can be used for de-cluttering large directories of e.g. photos.

At a high level, the algorithm is as follows:

Given a directory of files,

1. Bucket possible duplicates by file size. This metadata is cheaply available
   from the filesystem.
2. From those candidate duplicates, bucket them by the first N bytes, where N is
   ideally some small multiple of the block size of your disk, typically 512
   bytes or 4kb.
3. Optionally, repeat 2 for larger block sizes.
4. Do a full diff of the candidate duplicate files, and return all duplicated
   files.

The goal is to minimize the number of full diffs required. This algorithm
performs best with many files of different sizes and contents, e.g. photos. It
performs worst in the degenerate case where all files are equivalent, and we need
to read every byte of every file to determine that they are all the same.