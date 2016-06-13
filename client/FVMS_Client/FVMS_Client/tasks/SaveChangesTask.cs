﻿using FVMS_Client.files;
using FVMS_Client.tools;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FVMS_Client.tasks
{
    class SaveChangeTask : RequestTask
    {
        List<File>files;
        string message;
        public SaveChangeTask(List<File> files, String message) : base (Queues.QSaveChanges) {
            this.files = files;
            this.message = message;
    }

        public override Dictionary<string, object> prepareMessage()
        {
            Dictionary<string, object> response = new Dictionary<string, object>();
            int pid = files.First().pid;
            response.Add("files", files);
            response.Add("uid", LoggedUser.uid);
            response.Add("pid", pid);
            response.Add("message", message);
            return response;
        }

        public override void treatResponse(Dictionary<string, object> response)
        {
            object outobj;
            response.TryGetValue("authorized",out outobj);
            object newFiles;
            response.TryGetValue("newfiles", out newFiles);
            Dictionary<string, int> newFilesNames = JSONManipulator.DeserializeDict<String, int>(newFiles.ToString());
            int authorized = Int32.Parse(outobj.ToString());
            if (authorized==1)
            {
                foreach (File f in files)
                {
                    if (newFilesNames.Keys.Contains(f.path))
                    {
                        int fid;
                        newFilesNames.TryGetValue(f.path, out fid);
                        f.id = fid;
                    }
                    f.fileStatus = FileStatus.UPTODATE;
                }
            }
            else
            {
                FormsHandler.popup(Messages.Attention_NotAutorized);
            }
        }
    }
}
